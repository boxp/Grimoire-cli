(ns grimoire.commands
  (:use [clojure.repl :as repl]
        [grimoire.datas]
        [grimoire.oauth :as oauth])
  (:require [net.cgrand.enlive-html :as en])
  (:import (twitter4j TwitterFactory Query)
           (twitter4j.auth AccessToken)
           (twitter4j StatusUpdate)
           (javafx.scene.input Clipboard ClipboardContent)
           (javafx.application Application Platform)
           (javafx.scene Node Scene)
           (javafx.scene.input KeyCode)
           (javafx.scene.text Text Font FontWeight)
           (javafx.scene.control Label TextField PasswordField Button Hyperlink ListView)
           (java.lang Runnable)
           (javafx.scene.layout GridPane HBox VBox Priority)
           (javafx.scene.paint Color)
           (javafx.scene.image Image ImageView)
           (javafx.geometry Pos Insets)
           (javafx.event EventHandler)
           (javafx.stage Stage Modality)
           (javafx.collections FXCollections ObservableList)
           (javafx.fxml FXML FXMLLoader)
           (java.io File)))

;add javafx runlater thread
(defmacro add-runlater
  [body]
  `(Platform/runLater
     (reify Runnable
       (run [this]
         ~body))))

; コマンドたち
; ツイート
(defn post 
  " Post tweets \nUsage: (post \"hoge\")"
  [& input]
  (try 
    (str "Success:" 
      (.getText 
        (.updateStatus twitter 
          (if 
            (> (count (seq (apply str input))) 140)
              (str (apply str (take 137 (seq (apply str input)))) "...")
              (apply str input)))))
    (catch Exception e (println "Something has wrong." e))))

; 20件のツイート取得
(defn showtl 
  " Showing 20 new tweets from HomeTimeline.\nUsage: (showtl)"
  []
  (let [statusAll (reverse (.getHomeTimeline twitter))]
    (loop [status statusAll i 1]
      (if (= i 20)
        nil
        (do
          (println (.getScreenName (.getUser (first status))) ":" (.getText (first status)))
          (recur (rest status) (+ i 1)))))))

; コマンド一覧
(defn help []
  (str     "\n"
           "*** Grimoire-cli Commands List***\n"
           "post: Post tweets\n"
           "showtl: Showing 20 new Tweets from HomeTimeline.\n"
           "start: start userstream.\n"
           "stop: stop userstream.\n"
           "fav: Favorite status.\n"
           "retweet: Retweet status.\n"
           "favret: Retweet and Favorite status.\n"
           "reply: reply to tweet"
           "del: Delete tweet"
           "autofav!: Favoring"
           "print-node!: print 2 node"
           "Get more information to (doc <commands>)."))

; リツイート
(defn ret
  "Retweet Timeline's status number."
  [statusnum]
  (try 
    (let [status (.retweetStatus twitter (.getId (@tweets statusnum)))]
      (str 
        "Success retweet: @" 
        (.. status getUser getScreenName)
        " - "
        (.. status getText)))
    (catch Exception e "something has wrong.")))

; リツイートの取り消し
(defn unret
  "UnRetweet Timeline's status number."
  [statusnum]
  (try 
    (let [status (.destroyStatus twitter (.getId (@tweets statusnum)))]
      (str 
        "Success unretweet: @" 
        (.. status getUser getScreenName)
        " - "
        (.. status getText)))
    (catch Exception e "something has wrong.")))

; ふぁぼふぁぼ
(defn fav
  "Favorite Timeline's status number."
  [statusnum]
  (try
    (let [status (.createFavorite twitter (.getId (@tweets statusnum)))]
      (str
        "Success Fav: @" 
        (.. status getUser getScreenName)
        " - "
        (.. status getText)))
    (catch Exception e "something has wrong.")))

; あんふぁぼ
(defn unfav
  "Favorite Timeline's status number."
  [statusnum]
  (try
    (let [status (.destroyFavorite twitter (.getId (@tweets statusnum)))]
      (str
        "Success UnFav: @" 
        (.. status getUser getScreenName)
        " - "
        (.. status getText)))
    (catch Exception e "something has wrong.")))

; ふぁぼRT
; clean
(defn favret [statusnum]
  "Favorite and Retweet Timeline's status number"
  (do 
    (fav statusnum)
    (ret statusnum)))

; ふぁぼRT
; clean
(defn unfavret [statusnum]
  "UnFavorite and UnRetweet Timeline's status number"
  (do 
    (unfav statusnum)
    (unret statusnum)))

; つい消し
(defn del
  "Delete Timeline's status number."
  [statusnum]
  (try 
    (let [status (.destroyStatus twitter (.getId (@tweets statusnum)))]
      (str 
        "Success delete: @" 
        (.. status getUser getScreenName)
        " - "
        (.. status getText)))
    (catch Exception e "something has wrong.")))

; リプライ
(defn reply [statusnum & texts]
  "Reply to tweets"
  (let [reply (str \@ (.. (@tweets statusnum) getUser getScreenName) " " (apply str texts))]
    (do
      (println (str (apply str (take 137 (seq reply)))))
      (str "Success:" 
        (.getText
          (.updateStatus 
            twitter 
            (doto
              (StatusUpdate. 
                (if 
                  (> (count (seq reply)) 140)
                  (str (apply str (take 137 (seq reply))) "...")
                  (str \@ (.. (@tweets statusnum) getUser getScreenName) " " (apply str texts))))
              (.inReplyToStatusId (.getId (@tweets statusnum))))))))))

; gen-newstatus
(defn gen-newstatus [status]
  (let [newstatus {:user (.. status getUser getScreenName)
                   :text (.. status getText)
                   :time (.. status getCreatedAt)
                   :source (.. status getSource)
                   :inreply (.. status getInReplyToStatusId)
                   :isretweet? (.. status isRetweet)
                   :retweeted (.. status getRetweetCount)
                   :favorited? (.. status isFavorited)
                   :id (.. status getId)
                   :count (count @tweets)
                   :mentions (map #(.getScreenName %) (.. status getUserMentionEntities))
                   :url (map #(.getText %) (.. status getURLEntities))
                   :image (.. status getUser getBiggerProfileImageURL)}]
      newstatus))

; gen & add new status
; dirty
(defn add-newstatus! 
  [status]
  (do
    (dosync
      (alter tweets conj status))
    status))

; get-source from html
(defn get-source
  [source]
  (first 
    (:content 
      (first 
        (:content 
          (first 
            (:content
              (first
                (en/html-resource 
                    (java.io.StringReader.  source))))))))))


; gen-node
(defn gen-node! 
  [status]
  (let [node (doto (HBox.) 
                   (.setSpacing 10)
                   (.setPrefWidth 350))
        source (get-source (.. status getSource))
        image (Image. (.. status getUser getBiggerProfileImageURL) (double 73) (double 73) true true)
        imageview (doto (ImageView. image)
                    (.setFitHeight (double 48))
                    (.setFitWidth (double 48)))
        uname (doto (Text. (.. status getUser getScreenName))
                (.setId "profile")
                (.. wrappingWidthProperty (bind (.. @listv widthProperty (add -160)))))
        info (doto 
               (Text. 
                 (str (.. status getCreatedAt) " " source " " (.indexOf @tweets status)))
               (.setFont (Font. 10)))
        vbox (VBox.)
        downer (doto (HBox.)
               (.setSpacing 5))
        upper (doto (HBox.))
        favi-hover (ImageView. (Image. "favorite_hover.png" (double 16) (double 16) true true))
        favi-on (ImageView. (Image. "favorite_on.png" (double 16) (double 16) true true))
        favb (doto (Button.) 
               (.setGraphic
                 (if (.isFavorited status) 
                   favi-on
                   favi-hover)))
        reti-hover (ImageView. (Image. "retweet_hover.png" (double 16) (double 16) true true))
        reti-on (ImageView. (Image. "retweet_on.png" (double 16) (double 16) true true))
        retb (doto (Button.) 
               (.setGraphic
                 (if (.isRetweeted status) 
                   reti-on
                   reti-hover)))
        favretb (doto (Button.) 
               (.setGraphic
                 (if (.isFavorited status) 
                   reti-on
                   reti-hover)))
        text (doto (Text. (.getText status))
               (.setFont (Font. @tweets-size))
               (.. wrappingWidthProperty (bind (.. @listv widthProperty (add -80)))))]
    (do
      (doto favb
        (.setOnMouseClicked
          (proxy [EventHandler] []
            (handle [_]
              (add-runlater
                (if (.isFavorited status)
                  (do (unfav (.indexOf @tweets status))
                    (.setGraphic favb favi-hover))
                  (do (fav (.indexOf @tweets status))
                    (.setGraphic favb favi-on))))))))
      (doto retb
        (.setOnMouseClicked
          (proxy [EventHandler] []
            (handle [_]
              (add-runlater
                (if (.isRetweeted status)
                  (do (unret (.indexOf @tweets status))
                    (.setGraphic retb reti-hover))
                  (do (ret (.indexOf @tweets status))
                    (.setGraphic retb reti-on))))))))
      (doto (. downer getChildren)
        (.add info))
      (doto (. upper getChildren)
        (.add uname)
        (.add favb)
        (.add retb))
      (doto (. vbox getChildren) 
        (.add upper)
        (.add text)
        (.add downer))
      (doto (. node getChildren) 
        (.add imageview)
        (.add vbox)) 
      (HBox/setHgrow text Priority/SOMETIMES)
      (HBox/setHgrow vbox Priority/SOMETIMES)
      (HBox/setHgrow node Priority/SOMETIMES)
      (if 
        (some #(= (.. twitter getScreenName) %) (map #(.getText %) (.. status getUserMentionEntities)))
        (.. node (setId "mentions")))
      (add-runlater (.add @nodes 0 node))
      (add-runlater
        (if (> (.size @nodes) @max-nodes)
          (.remove @nodes @max-nodes (.size @nodes)))))))

; print 2 nodes
(defn print-node!
  [st & sts] 
  (add-runlater
    (let [lbl (doto (Text. (str st (apply str sts)))
                (.. wrappingWidthProperty (bind (.. @listv widthProperty (add -80))))
                (.setFont (Font. @tweets-size)))]
      (.add @nodes 0 lbl))))

; set-theme
(defn set-theme
  "set theme"
  [name]
  (.. @mainscene getStylesheets 
    (add 
      (str name ".css"))))

(defn autofav!
  "ふぁぼぉおおおおおおおおおおお"
  [& user]
  (dosync 
    (ref-set on-status 
      (fn [status] 
        (if (some #(= %  (.. status getUser getScreenName)) user) 
          (fav 
            (.indexOf @tweets status)))))))

(defn get-home
  []
  (let [home (System/getenv "HOME")]
    (if home
      home
      (System/getProperty "user.home"))))

(defn search
  [& strs]
  (map gen-node!
    (map add-newstatus!
      (reverse
        (.. twitter (search (Query. (apply str strs))) getTweets)))))

; デバック用
(defn reload []
  (do
    (load-file "src/grimoire/commands.clj")
    (load-file "src/grimoire/response.clj")))
