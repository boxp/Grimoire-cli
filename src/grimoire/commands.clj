(ns grimoire.commands
  (:use [clojure.java.shell]
        [clojure.string :only [join split]]
        [clojure.repl :as repl]
        [clojure.java.io]
        [grimoire.plugin]
        [grimoire.data]
        [grimoire.oauth :as oauth])
  (:require [net.cgrand.enlive-html :as en])
  (:import (twitter4j TwitterFactory Query)
           (twitter4j.auth AccessToken)
           (twitter4j StatusUpdate)
           (javafx.scene.input Clipboard ClipboardContent KeyCode KeyCodeCombination KeyCombination KeyCombination$Modifier)
           (javafx.application Application Platform)
           (javafx.scene Node Scene)
           (javafx.scene.text Text Font FontWeight)
           (javafx.scene.control Label TextField TextArea PasswordField Button Hyperlink ListView)
           (javafx.scene.web WebView)
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

(defn get-node
  "Search and get node with st."
  [st]
  (.. @main-stage getScene (lookup st)))

(defn bool-dialog
  "Generate bool type dialog."
  [q pos func neg]
  (let [lblq (doto (Label. q)
               (.setId "label"))
        posbtn (doto (Button. pos)
                 (.setId "button"))
        negbtn (doto (Button. neg)
                 (.setId "button"))
        body (doto (GridPane.)
               (.setId "maintl")
               (.add lblq 1 1)
               (.add posbtn 1 2)
               (.add negbtn 2 2)
               (.setHgap 10))
        scene (doto (Scene. body 480 240)
                (.. getStylesheets (add (str @theme ".css"))))
        stage (doto (Stage.)
                (.setTitle "Grimoire - dialog")
                (.setScene scene))]
    (do
      (.setOnAction posbtn
        (proxy [EventHandler] []
          (handle [_]
            (do
              (.close stage)))))
      (GridPane/setMargin posbtn 
        (Insets. 10 0 10 10))
      (GridPane/setMargin negbtn 
        (Insets. 10 10 10 0))
      (GridPane/setMargin lblq 
        (Insets. 10 10 0 10))
      (.show stage)
      (.setOnAction negbtn
        (proxy [EventHandler] []
          (handle [_]
            (.close stage))))
      (.setOnAction posbtn
        (proxy [EventHandler] []
          (handle [_]
            (do
              (load-string func)
              (.close stage))))))))

(defn selected-status
  "Get selected tweet"
  []
  (@tweet-maps
    (first 
      (.. (get-node "#tllv")  getSelectionModel getSelectedItems))))

(defn focused-status
  "Get focused tweet"
  []
  (@tweet-maps
    (.. (get-node "#tllv") getFocusModel getFocusedItem)))

;add javafx runlater thread
(defmacro add-runlater
  "Add parameter to Platform/runlater"
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
    (future
      (str "Success:" 
        (.getText 
          (.updateStatus twitter 
            (if 
              (> (count (seq (apply str input))) 140)
                (str (apply str (take 137 (seq (apply str input)))) "...")
                (apply str input))))))
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
  (str     "*** Grimoire-cli Commands List***\n\n"
           "post: Post tweets\n"
           "showtl: Showing 20 new Tweets from HomeTimeline.\n"
           "start: start userstream.\n"
           "stop: stop userstream.\n"
           "fav: Favorite status.\n"
           "retweet: Retweet status.\n"
           "favret: Retweet and Favorite status.\n"
           "reply: reply to tweet\n"
           "del: Delete tweet\n"
           "autofav!: Favoring\n"
           "follow: follow user by statusnum.\n"
           "open-url: Open webview from statusnum.\n"
           "print-node!: print 2 node\n"
           "Get more information to (doc <commands>)."))

; リツイート
(defn ret
  "Retweet Timeline's status number."
  ([]
    (future
      (try 
        (let [status (selected-status)]
          (str 
            "Success retweet: @" 
            (.. status getUser getScreenName)
            " - "
            (.. status getText)))
        (catch Exception e "something has wrong."))))
  ([statusnum]
    (future
      (try 
        (let [status (.retweetStatus twitter (.getId (@tweets statusnum)))]
          (str 
            "Success retweet: @" 
            (.. status getUser getScreenName)
            " - "
            (.. status getText)))
        (catch Exception e "something has wrong.")))))

; リツイートの取り消し
(defn unret
  "UnRetweet Timeline's status number."
  ([]
    (future
      (try 
        (let [status (.destroyStatus twitter (.getId (selected-status)))]
          (str 
            "Success unretweet: @" 
            (.. status getUser getScreenName)
            " - "
            (.. status getText)))
        (catch Exception e "something has wrong."))))
  ([statusnum]
    (future
      (try 
        (let [status (.destroyStatus twitter (.getId (@tweets statusnum)))]
          (str 
            "Success unretweet: @" 
            (.. status getUser getScreenName)
            " - "
            (.. status getText)))
        (catch Exception e "something has wrong.")))))

; ふぁぼふぁぼ
(defn fav
  "Favorite Timeline's status number."
  ([]
    (future
      (try
        (let [status (.createFavorite twitter (.getId (selected-status)))]
          (str
            "Success Fav: @" 
            (.. status getUser getScreenName)
            " - "
            (.. status getText)))
        (catch Exception e "something has wrong."))))
  ([statusnum]
    (future
      (try
        (let [status (.createFavorite twitter (.getId (@tweets statusnum)))]
          (str
            "Success Fav: @" 
            (.. status getUser getScreenName)
            " - "
            (.. status getText)))
        (catch Exception e "something has wrong.")))))

; あんふぁぼ
(defn unfav
  "Favorite Timeline's status number."
  ([]
    (future
      (try
        (let [status (.destroyFavorite twitter (.getId (selected-status)))]
          (str
            "Success UnFav: @" 
            (.. status getUser getScreenName)
            " - "
            (.. status getText)))
        (catch Exception e "something has wrong."))))
  ([statusnum]
    (future
      (try
        (let [status (.destroyFavorite twitter (.getId (@tweets statusnum)))]
          (str
            "Success UnFav: @" 
            (.. status getUser getScreenName)
            " - "
            (.. status getText)))
        (catch Exception e "something has wrong.")))))

; ふぁぼRT
; clean
(defn favret 
  "Favorite and Retweet Timeline's status number"
  ([]
    (do 
      (fav)
      (ret)))
  ([statusnum]
    (do 
      (fav statusnum)
      (ret statusnum))))

; ふぁぼRT
; clean
(defn unfavret 
  "UnFavorite and UnRetweet Timeline's status number"
  ([]
    (do 
      (unfav)
      (unret)))
  ([statusnum]
    (do 
      (unfav statusnum)
      (unret statusnum))))

; つい消し
(defn del
  "Delete Timeline's status number."
  ([]
    (try 
      (let [status (.destroyStatus twitter (.getId (selected-status)))]
        (str 
          "Success delete: @" 
          (.. status getUser getScreenName)
          " - "
          (.. status getText)))
      (catch Exception e "something has wrong.")))
  ([statusnum]
    (try 
      (let [status (.destroyStatus twitter (.getId (selected-status)))]
        (str 
          "Success delete: @" 
          (.. status getUser getScreenName)
          " - "
          (.. status getText)))
      (catch Exception e "something has wrong."))))

; リプライ
(defn reply [statusnum & texts]
  "Reply to tweets"
  (let [reply (str \@ (.. (@tweets statusnum) getUser getScreenName) " " (apply str texts))]
    (future
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
                (.inReplyToStatusId (.getId (@tweets statusnum)))))))))))

; gen-newstatus
(defn gen-newstatus [status]
  "Gen newstauts data type(halted)"
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

; dirty
(defn add-newstatus! 
  "Add status to tweets."
  [status]
  (do
    (dosync
      (alter tweets conj status))
    status))

; get-source from html
(defn get-source
  "Get \"via name\" from Status/source."
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
  "Generate node and add to listview."
  [status]
  (future 
    (let [statusnum (.indexOf @tweets status)
          node (doto (proxy [HBox] []
                       (getStatusNum [] ~statusnum)) 
                     (.setSpacing 10)
                     (.setPrefWidth 350))
          source (if (.. status isRetweet)
                   (get-source (.. status getRetweetedStatus getSource))
                   (get-source (.. status getSource)))
          image (if (.. status isRetweet)
                  (Image. (.. status getRetweetedStatus getUser getBiggerProfileImageURL) (double 73) (double 73) true true)
                  (Image. (.. status getUser getBiggerProfileImageURL) (double 73) (double 73) true true))
          imageview (doto (ImageView. image)
                      (.setFitHeight (double 48))
                      (.setFitWidth (double 48)))
          uname (if (.. status isRetweet)
                  (doto (Text. (str (.. status getRetweetedStatus getUser getScreenName) " Retweeted by " (.. status getUser getScreenName)))
                    (.setId "profile")
                    (.. wrappingWidthProperty (bind (.. (get-node "#tllv") widthProperty (add -160)))))
                  (doto (Text. (.. status getUser getScreenName))
                    (.setId "profile")
                    (.. wrappingWidthProperty (bind (.. (get-node "#tllv") widthProperty (add -160))))))
          info (if (.. status isRetweet)
                 (doto 
                   (Text. 
                     (str (.. status getRetweetedStatus getCreatedAt) " " source " " statusnum))
                   (.setFont (Font. 10)))
                 (doto 
                   (Text. 
                     (str (.. status getCreatedAt) " " source " " statusnum))
                   (.setFont (Font. 10))))
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
          repi (ImageView. (Image. "reply_hover.png" (double 16) (double 16) true true))
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
          repb (doto (Button.)
                 (.setGraphic repi))
          text (if (.. status isRetweet)
                 (doto (Text. (.. status getRetweetedStatus getText))
                   (.setFont (Font. @tweets-size))
                   (.. wrappingWidthProperty (bind (.. (get-node "#tllv") widthProperty (add -80)))))
                 (doto (Text. (.. status getText))
                   (.setFont (Font. @tweets-size))
                   (.. wrappingWidthProperty (bind (.. (get-node "#tllv") widthProperty (add -80))))))
          tweet-map {node status}]
      (do
        ; Listeners setting
        (doto favb
          (.setOnMouseClicked
            (proxy [EventHandler] []
              (handle [_]
                (add-runlater
                  (if (.isFavorited status)
                    (do (unfav statusnum)
                      (.setGraphic favb favi-hover))
                    (do (fav statusnum)
                      (.setGraphic favb favi-on))))))))
        (doto retb
          (.setOnMouseClicked
            (proxy [EventHandler] []
              (handle [_]
                (add-runlater
                  (if (.isRetweeted status)
                    (do (unret statusnum)
                      (.setGraphic retb reti-hover))
                    (do (ret statusnum)
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
        (dosync
          (alter tweet-maps merge tweet-map))
        ; メンションツイートか否か
        (if 
          (some #(= (.. twitter getScreenName) %) (map #(.getText %) (.. status getUserMentionEntities)))
          (.. node (setId "mentions")))
        ; Nodeをtweetsへ追加
        (add-runlater (.add @nodes 0 node))
        (add-runlater
          (if (> (.size @nodes) @max-nodes)
            (.remove @nodes @max-nodes (.size @nodes))))))))

; print 2 nodes
(defn print-node!
  "Print sts to listview."
  [st & sts] 
  (add-runlater
    (let [lbl (doto (Text. (str st (apply str sts)))
                (.. wrappingWidthProperty (bind (.. (get-node "#tllv") widthProperty (add -80))))
                (.setFont (Font. @tweets-size)))]
      (.add @nodes 0 lbl))))

; set-theme
(defn set-theme
  "set theme. (Testing.)"
  [name]
  (.. @mainscene getStylesheets 
    (add 
      (str name ".css"))))

(defn autofav!
  "Add user to autofav list.(crazy)"
  [& user]
  (let [plugin (reify Plugin
                 (on-status [_ status] 
                   (if (some #(= %  (.. status getUser getScreenName)) user) 
                    (fav 
                      (.indexOf @tweets status)))))]
    (dosync
      (alter plugins conj plugin))))

(defn get-home
  "Return home directory."
  []
  (let [home (System/getenv "HOME")]
    (if home
      home
      (System/getProperty "user.home"))))

(defn search
  "Search public time line by strs and show to listview."
  [& strs]
  (doall
    (map gen-node!
      (reverse
        (.. twitter (search (Query. (apply str (join " " strs)))) getTweets)))))

(defn gvim
  "Edit input field from gvim"
  ([]
    (binding [*ns* (find-ns 'grimoire.gui)]
      (future 
        (try
          (do
            (sh "gvim" 
              (str (get-home) "/.grimoire/.tmp")))
            (print-node!
              (load-file 
                (str (get-home) "/.grimoire/.tmp")))
          (catch Exception e (print-node! e))))))
  ([adr]
    (binding [*ns* (find-ns 'grimoire.gui)]
      (future 
        (try
          (do
            (sh "gvim" adr))
            (print-node!
              (load-file adr))
          (catch Exception e (print-node! e)))))))

(defn input-form
  "Open evalation form"
  []
  (let [txta (doto (TextArea.)
               (.setMaxHeight 2000))
        btn (doto (Button. "Eval")
              (.setId "button")
              (.setMaxWidth 2000)
              (.setOnAction
                (proxy [EventHandler] []
                  (handle [_]
                    (binding [*ns* (find-ns 'grimoire.gui)]
                      (print-node! 
                        (try 
                          (load-string (.getText txta))
                          (catch Exception e (.getMessage e)))))))))
        body (doto (VBox. 5)
               (.setPadding (Insets. 5 5 5 5))
               (.setId "maintl")
               (.. getChildren (add txta))
               (.. getChildren (add btn)))
        scene (doto (Scene. body 300 300)
                (.. getStylesheets (add (str @theme ".css"))))
        stage (doto (Stage.)
                (.setScene scene)
                (.setTitle "Grimoire - Evalation form"))]
    (do
      (VBox/setVgrow txta Priority/ALWAYS)
      (HBox/setHgrow btn Priority/ALWAYS)
      (.setOnKeyPressed txta
        (proxy [EventHandler] []
          (handle [ke]
            (cond 
              (and 
                (= (. ke getCode) KeyCode/E)
                (. ke isControlDown))
              (.fire btn)
              (and
                (= (.. ke getCode getName) "ENTER")
                (. ke isControlDown))
              (.fire btn)))))
      (. stage show))))
    
(defn gen-webview
  "Gen webview from url."
  [url]
  (let [webview (WebView.)
        engine (doto (. webview getEngine)
                 (.load url))]
    (doto (Stage.)
      (.setScene (Scene. webview 800 600))
      (.show))))

(defn url
  "Open url with web view"
  ([] 
    (let [status (selected-status)
          text (split (.getText status) #" ")
          urls (filter #(= (seq "http") (take 4 %)) text)]
      (doall
        (map #(gen-webview %) urls))))
  ([statusnum] 
    (let [status (@tweets statusnum)
          text (split (.getText status) #" ")
          urls (filter #(= (seq "http") (take 4 %)) text)]
      (doall
        (map #(gen-webview %) urls)))))

(defn browse
  "Open url with browser"
  ([] 
    (let [status (selected-status)
          text (split (.getText status) #" ")
          urls (filter #(= (seq "http") (take 4 %)) text)]
      (doall
        (map #(sh @browser %) urls))))
  ([statusnum] 
    (let [status (@tweets statusnum)
          text (split (.getText status) #" ")
          urls (filter #(= (seq "http") (take 4 %)) text)]
      (doall
        (map #(sh @browser %) urls)))))

(defn follow
  ([]
    (let [status (selected-status)]
      (.createFriendship twitter (.. status getUser getId) true)))
  ([statusnum]
    (let [status (@tweets statusnum)]
      (.createFriendship twitter (.. status getUser getId) true))))

(defn gen-keycombi
  [keycode & modifiers]
    (KeyCodeCombination. keycode (into-array KeyCombination$Modifier modifiers)))

; デバック用
(defn reload 
  ([]
    (do
      (load-file (str (get-home) "/.grimoire.clj"))
      (load-file (str (get-home) "/Dropbox/program/clojure/grimoire-cli/src/grimoire/commands.clj"))
      (load-file (str (get-home) "/Dropbox/program/clojure/grimoire-cli/src/grimoire/plugin.clj"))
      (.setRoot (.getScene @main-stage)
        (-> "main.fxml" resource FXMLLoader/load))
      (use 'grimoire.commands)
      (use 'grimoire.plugin)))
  ([file]
    (do
      (load-file (str (get-home) "/Dropbox/program/clojure/grimoire-cli/src/grimoire/" file ".clj")))))
