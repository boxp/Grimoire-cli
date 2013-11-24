(ns grimoire.commands
  (:use [clojure.java.shell]
        [clojure.string :only [join split]]
        [clojure.repl :as repl]
        [clojure.java.io]
        [grimoire.data]
        [grimoire.oauth]
        [grimoire.services]
        [grimoire.wrapper])
  (:require [net.cgrand.enlive-html :as en])
  (:import (twitter4j TwitterFactory Query Status User UserMentionEntity)
           (twitter4j.auth AccessToken)
           (twitter4j StatusUpdate)
           (javafx.scene.input Clipboard ClipboardContent KeyCode KeyCodeCombination KeyCombination KeyCombination$Modifier)
           (javafx.application Application Platform)
           (javafx.scene Node Scene)
           (javafx.scene.text Text Font FontWeight)
           (javafx.scene.control Label TextField TextArea PasswordField Button Hyperlink ListView MenuItem)
           (javafx.scene.web WebView)
           (java.lang Runnable)
           (java.util Date)
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
  "scene から css id でノードを検索して返す"
  [st]
  (.. @mainscene (lookup st)))

(defn get-master-pane
  "一番初めに作られたpane(panesの一番目の要素)を返します"
  []
  (.. (get-node "#pane") getChildren (get 0)))

(defn bool-dialog
  "OKボタン，Cancelボタン，質問からなる確認ダイアログを作成，表示します．引数q:質問,引数pos:OKボタンに設定するテキスト,引数f:OKボタンを押した時に実行される関数,引数neg:Cancelボタンのテキスト"
  [q pos f neg]
  (let [lblq (doto (Label. q)
               (.setFont (Font. 20))
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
        scene (doto (Scene. body)
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
              (f)
              (.close stage))))))))

(defn form-dialog
  "入力ダイアログの作成，表示，title:タイトル,q:メッセージ,pos:肯定ボタンのテキスト,f:肯定ボタンを押された時に呼ばれる関数,neg:否定ボタンのテキスト"
  [title q pos f neg]
  (let [lblq (doto (Label. q)
               (.setFont (Font. 20))
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
        scene (doto (Scene. body)
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
              (f)
              (.close stage))))))))
(defn selected-status
  "選択されているツイートのStatusを取得"
  ([]
    (@tweet-maps (.. (.getFocusOwner (.getScene @main-stage))  getSelectionModel getSelectedItems)))
  ([lv]
    (@tweet-maps (.. lv  getSelectionModel getSelectedItems))))

(defn focused-status
  "フォーカス中のツイートのStatusを取得"
  ([]
    (@tweet-maps (.. (.getFocusOwner (.getScene @main-stage)) getFocusModel getFocusedItem)))
  ([lv]
    (@tweet-maps (.. lv getFocusModel getFocusedItem))))

;add javafx runlater thread
(defmacro add-runlater
  "引数を非同期で実行(JavaFxのPlatform/runlaterに登録)"
  [body]
  `(Platform/runLater
     (reify Runnable
       (run [this]
         ~body))))

; コマンドたち
; ツイート
(defn post 
  "引数の文字列を全て一つにまとめてツイートする．140文字以上の時は省略されます．"
  [& input]
  (try 
    (str "Success:" 
      (.getText 
        (.updateStatus @twitter 
          (if 
            (> (count (apply str input)) 140)
              (apply str (take 137 (apply str input)) "...")
              (apply str input)))))
    (catch Exception e (str "Something has wrong." (.getMessage e)))))

; 20件のツイート取得
(defn showtl 
  " Showing 20 new tweets from HomeTimeline.\nUsage: (showtl)"
  []
  (let [statusAll (reverse (.getHomeTimeline @twitter))]
    (loop [status statusAll i 1]
      (if (= i 20)
        nil
        (do
          (println (.getScreenName (.getUser (first status))) ":" (.getText (first status)))
          (recur (rest status) (+ i 1)))))))

; コマンド一覧
(defn help []
  (str     "*** Grimoire-cli Commands List***\n\n"
           "post: ツイート(例：(post \"test\"))\n"
           "start: ユーザーストリームをスタートさせる.\n"
           "stop: ユーザーストリームをストップさせる.\n"
           "fav: ふぁぼる(例：(fav 2))\n"
           "retweet: リツイートする(例：(ret 3))\n"
           "favret: ふぁぼってリツイートする(例：(favret 6))\n"
           "reply: リプライを送る(例：(reply 1 \"hoge\"))\n"
           "del: ツイートを削除(例：(del 168))\n"
           "autofav!: 指定したユーザーのツイートを自動でふぁぼる(例：(autofav! \"@If_I_were_boxp\"))\n"
           "follow: ツイートのユーザーをフォローする(例：(follow 58))\n"
           "open-url: ツイートのURLをブラウズする（例：(open-url: 19)\n"
           "print-node!: テキストをタイムラインに表示します（例：(print-node! \"Too late.\"))\n"
           "Get more information to (doc <commands>)."))

; リツイート
(defn ret
  "statusnum(ツイートの右下に表示)を指定してリツイート"
  ([]
    (future
      (try 
        (let [status (focused-status (.. @main-stage getScene getFocusOwner))]
          (str 
            "Success retweet: @" 
            (.. status getUser getScreenName)
            " - "
            (.. status getText)))
        (catch Exception e "something has wrong."))))
  ([statusnum]
    (future
      (try 
        (let [status (.retweetStatus @twitter (.getId (@tweets statusnum)))]
          (str 
            "Success retweet: @" 
            (.. status getUser getScreenName)
            " - "
            (.. status getText)))
        (catch Exception e "something has wrong.")))))

; リツイートの取り消し
(defn unret
  "statusnum(ツイートの右下に表示)を指定してリツイートを取り消し"
  ([]
    (future
      (try 
        (let [status (.destroyStatus @twitter (.getId (focused-status (.. @main-stage getScene getFocusOwner))))]
          (str 
            "Success unretweet: @" 
            (.. status getUser getScreenName)
            " - "
            (.. status getText)))
        (catch Exception e "something has wrong."))))
  ([statusnum]
    (future
      (try 
        (let [status (.destroyStatus @twitter (.getId (@tweets statusnum)))]
          (str 
            "Success unretweet: @" 
            (.. status getUser getScreenName)
            " - "
            (.. status getText)))
        (catch Exception e "something has wrong.")))))

; ふぁぼふぁぼ
(defn fav
  "statusnum(ツイートの右下に表示)を指定してふぁぼ"
  ([]
    (future
      (try
        (let [status (.createFavorite @twitter (.getId (focused-status (.. @main-stage getScene getFocusOwner))))]
          (str
            "Success Fav: @" 
            (.. status getUser getScreenName)
            " - "
            (.. status getText)))
        (catch Exception e "something has wrong."))))
  ([statusnum]
    (future
      (try
        (let [status (.createFavorite @twitter (.getId (@tweets statusnum)))]
          (str
            "Success Fav: @" 
            (.. status getUser getScreenName)
            " - "
            (.. status getText)))
        (catch Exception e "something has wrong.")))))

; あんふぁぼ
(defn unfav
  "statusnum(ツイートの右下に表示)を指定してあんふぁぼ"
  ([]
    (future
      (try
        (let [status (.destroyFavorite @twitter (.getId (focused-status (.. @main-stage getScene getFocusOwner))))]
          (str
            "Success UnFav: @" 
            (.. status getUser getScreenName)
            " - "
            (.. status getText)))
        (catch Exception e "something has wrong."))))
  ([statusnum]
    (future
      (try
        (let [status (.destroyFavorite @twitter (.getId (@tweets statusnum)))]
          (str
            "Success UnFav: @" 
            (.. status getUser getScreenName)
            " - "
            (.. status getText)))
        (catch Exception e "something has wrong.")))))

; ふぁぼRT
; clean
(defn favret 
  "statusnum(ツイートの右下に表示)を指定してふぁぼ＆リツイート"
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
  "statusnum(ツイートの右下に表示)を指定してふぁぼ＆リツイートを取り消す"
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
  "statusnum(ツイートの右下に表示)を指定してツイートを取り消す"
  ([]
    (try 
      (let [status (focused-status (.. @main-stage getScene getFocusOwner))]
        (do
          (.destroyStatus @twitter (.getId status))
          (str 
            "Success delete: @" 
            (.. status getUser getScreenName)
            " - "
            (.. status getText))))
      (catch Exception e "something has wrong.")))
  ([statusnum]
    (try 
      (let [status (@tweets statusnum)]
        (do
          (.destroyStatus @twitter (.getId status))
          (str 
            "Success delete: @" 
            (.. status getUser getScreenName)
            " - "
            (.. status getText))))
      (catch Exception e "something has wrong."))))

; リプライ
(defn reply [statusnum & texts]
  "statusnum(ツイートの右下に表示)とテキストを指定して,返信する"
  (let [reply (str \@ (.. (@tweets statusnum) getUser getScreenName) " " (apply str texts))]
    (future
      (do
        (println (str (apply str (take 137 (seq reply)))))
        (str "Success:" 
          (.getText
            (.updateStatus 
              @twitter 
              (doto
                (StatusUpdate. 
                  (if 
                    (> (count (seq reply)) 140)
                    (str (apply str (take 137 (seq reply))) "...")
                    (str \@ (.. (@tweets statusnum) getUser getScreenName) " " (apply str texts))))
                (.inReplyToStatusId (.getId (@tweets statusnum)))))))))))

; get-source from html
(defn get-source
  "twitter4j.Status.getSourceのhtmlをテキストに変換"
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
  "twitter4j.Statusから画面に表示するnodeを作成し，返す"
  [#^Status status]
  #^HBox
  (let [statusnum ^int (.indexOf @tweets status)
        node (doto (HBox.) 
             (.setSpacing 10)
             (.setPrefWidth 350))
        source (cond ^boolean (. status isRetweet)
                 (get-source (.. status getRetweetedStatus getSource))
                 (= (.. status getUser getScreenName) "Grimoire")
                 (.. status getSource)
                 :else
                 (get-source (.. status getSource)))
        url (.. status getUser getBiggerProfileImageURL)
        image (if (. status isRetweet)
                (let [returl (.. status getRetweetedStatus getUser getBiggerProfileImageURL)]
                  (or 
                    (@imagemap returl)
                    (do
                      (dosync
                        (alter imagemap merge 
                          {returl (Image. returl true)}))
                      (@imagemap returl))))
                (or 
                  (@imagemap url)
                  (do
                    (dosync
                      (alter imagemap merge 
                        {url (Image. url true)}))
                    (@imagemap url))))
        imageview (doto (ImageView. image)
                    (.setFitHeight (double 48))
                    (.setFitWidth (double 48)))
        uname (if (.. status isRetweet)
                (doto (Text. (str (.. status getRetweetedStatus getUser getScreenName) " Retweeted by " (.. status getUser getScreenName)))
                  (.setId "profile")
                  (.. wrappingWidthProperty (bind (.. (get-master-pane) widthProperty (add -160)))))
                (doto (Text. (.. status getUser getScreenName))
                  (.setId "profile")
                  (.. wrappingWidthProperty (bind (.. (get-master-pane) widthProperty (add -160))))))
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
        favi-hover (ImageView. (Image. "favorite_hover.png" (double 12) (double 12) true true))
        favi-on (ImageView. (Image. "favorite_on.png" (double 12) (double 12) true true))
        favb (doto (Button.) 
               (.setGraphic
                 (if (.isFavorited status) 
                   favi-on
                   favi-hover)))
        reti-hover (ImageView. (Image. "retweet_hover.png" (double 12) (double 12) true true))
        reti-on (ImageView. (Image. "retweet_on.png" (double 12) (double 12) true true))
        repi (ImageView. (Image. "reply_hover.png" (double 12) (double 12) true true))
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
                 (.. wrappingWidthProperty (bind (.. (get-master-pane) widthProperty (add -80)))))
               (doto (Text. (.. status getText))
                 (.setFont (Font. @tweets-size))
                 (.. wrappingWidthProperty (bind (.. (get-master-pane) widthProperty (add -80))))))]
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
      ; メンションツイートか否か
      (if 
        (some #(= myname %) (map #(.getText %) (.. status getUserMentionEntities)))
        (.. node (setId "mentions")))
      node)))



; set-theme
(defn set-theme!
  "Grimoire全体のテーマを設定します(solarized_darkなど)"
  [name]
   (do
     (reset! theme (str name ".css"))
     (.. (.getScene @main-stage) getStylesheets 
       (add 
         (str name ".css")))))

(defn clear-theme!
  "Grimoire全体のテーマをリセットします"
  []
  (do
    (.. @main-stage getScene getStylesheets clear)
    (reset! theme nil)))

(defn autofav!
  "引数のユーザーを自動でふぁぼる(マジキチ向け)"
  [& user]
  (let [plugin (reify Plugin
                 (on-status [_ status] 
                   (if (some #(= %  (.. status getUser getScreenName)) user) 
                    (fav 
                      (.indexOf @tweets status)))))]
    (dosync
      (alter plugins conj plugin))))

(defn get-home
  "ホームディレクトリを取得"
  []
  (let [home (System/getenv "HOME")]
    (if home
      home
      (System/getProperty "user.home"))))

(defn search
  "引数の文字列からツイートを検索し，twitter4j.Statusで返す．"
  [& strs]
    (reverse
      (.. @twitter (search (Query. (apply str (join " " strs)))) getTweets)))

(defn gen-webview
  "引数のURLをWebViewでブラウズ"
  [url]
  (let [webview (WebView.)
        engine (doto (. webview getEngine)
                 (.load url))]
    (doto (Stage.)
      (.setScene (Scene. webview 800 600))
      (.setTitle (.getLocation engine))
      (.show))))

(defn url
  "statusnum(ツイートの右下に表示)を指定して，ツイートの中のURLをWebViewでブラウズ"
  ([] 
    (let [status (focused-status (.. @main-stage getScene getFocusOwner))
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
  "statusnum(ツイートの右下に表示)を指定して，ツイートの中のURLをbrowserに指定したブラウザでブラウズ"
  ([] 
    (let [status (focused-status (.. @main-stage getScene getFocusOwner))
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
  "statusnum(ツイートの右下に表示)を指定して，指定したツイートのユーザーをフォローする"
  ([]
    (let [status (focused-status (.. @main-stage getScene getFocusOwner))]
      (if (.isRetweet status)
        (.createFriendship @twitter (.. status getRetweetedStatus getUser getId) true)
        (.createFriendship @twitter (.. status getUser getId) true))))
  ([statusnum]
    (let [status (@tweets statusnum)]
      (if (.isRetweet status)
        (.createFriendship @twitter (.. status getRetweetedStatus getUser getId) true)
        (.createFriendship @twitter (.. status getUser getId) true)))))


(defn get-selected-urls
  "フォーカスしているツイートに含まれるURLのリストを返す．"
  []
  (let [text (split (.. (focused-status (.. @main-stage getScene getFocusOwner)) getText) #" ")
        urls (filter #(= (seq "http") (take 4 %)) text)]
    urls))

(defn add-nodes!
  "statusからnodeを生成してcoll(observablearraylist)に追加します"
  [coll status]
  (let [item (gen-node! status)]
    (do
      (add-runlater
        (.add coll 0 item))
      (dosync
        (alter tweet-maps merge {item status})))))

; print 2 nodes
(defn gen-notice
  "文字列からtwitter4j.Statusを生成して返す"
  [st & sts] 
  (let [grimoire (proxy [User] []
                   (getScreenName []
                     "Grimoire")
                   (getMiniProfileImageURL []
                     "alice.png")
                   (getBiggerProfileImageURL []
                     "alice.png"))
        status (proxy [Status] []
                 (isRetweet []
                   false)
                 (isRetweeted []
                   false)
                 (isFavorited []
                   false)
                 (getUser []
                   grimoire)
                 (getSource [] 
                   "Grimoire(clojure)")
                 (getCreatedAt []
                   (Date.))
                 (getId [] 0)
                 (getText []
                   (apply str (conj sts st)))
                 (getUserMentionEntities [] (into-array UserMentionEntity [])))]
    status))

; print 2 nodes
(defn print-node!
  "引数のテキストをタイムラインに表示する"
  [st & sts] 
  (let [grimoire (proxy [User] []
                   (getScreenName []
                     "Grimoire")
                   (getBiggerProfileImageURL []
                     "alice.png"))
        status (proxy [Status] []
                 (isRetweet []
                   false)
                 (isRetweeted []
                   false)
                 (isFavorited []
                   false)
                 (getUser []
                   grimoire)
                 (getSource [] 
                   "Grimoire(clojure)")
                 (getCreatedAt []
                   (Date.))
                 (getId [] 0)
                 (getText []
                   (apply str (conj sts st)))
                 (getUserMentionEntities [] (into-array UserMentionEntity [])))
        node (gen-node! status)]
    (do
      (add-runlater
        (.add nodes 0 node))
      (add-runlater
        (if (>= (.size nodes) @max-nodes) 
          (.remove nodes (dec @max-nodes) (.size nodes))))
       node)))

(defn gvim
  "gvimを立ち上げ，保存した文字列をEvalします．(引数を指定するとファイル名を指定)"
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
  "Evalation formを開く"
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

(defn check-update
  "Grimoireのアップデートをチェック"
  []
  (future
    (let [cache (slurp "http://archbox.dip.jp/works/grimoire/Grimoire.jar")
          local (slurp (System/getProperty "java.class.path"))]
       (if (= cache local)
         (print-node! "Grimoireは最新です")
         (do
           (bool-dialog "最新版のGrimoireが公開されています，Updateしますか？" "はい"
             #(spit (System/getProperty "java.class.path") cache)
             "お断りします")
           (print-node! "最新版にアップデートしました."))))))

(defn open-twitter-signup!
  "oauthtokenを更新し，Twitterの認証画面を開く"
  [] 
  (let [_ (get-oauthtoken!)
        twitter-url (. @oauthtoken getAuthorizationURL)]
    (gen-webview twitter-url)))

(defn refresh-profileimg!
  "画面左下の現在のアカウント画像を更新します"
  []
  (add-runlater
    (.setImage (get-node "#profileimg") 
      (Image. (.. @twitter (showUser @myname) getBiggerProfileImageURL)))))

(defn select-acount!
  "メインアカウントをacount(keyword)に変更します"
  [acount]
  (let [target (@twitters acount)
        target-name (. target getScreenName)]
    (reset! twitter target)
    (reset! myname target-name)
    (refresh-profileimg!)))

(defn delete-acount!
  "アカウントを削除"
  [acount]
  (let [target-tabs (@tabs acount)
        maintl-list (.. (get-node "#HomeTimeline") getTabs)
        mention-list (.. (get-node "#Mentions") getTabs)
        twitterstreamins (acount @twitterstreams)]
    (do
      ; userstreamの停止
      (. twitterstreamins shutdown)
      ; tokenの削除
      (dosync
        (alter subtokens dissoc acount))
      (spit (str (get-home) "/.grimoire/subtokens.clj")
        @subtokens)
      ; tabの消去
      (. maintl-list (remove (target-tabs 0)))
      (. mention-list (remove (target-tabs 1))))))

; デバック用
(defn reload 
  ([]
    "プロジェクトのソースコードをリロードします．(デバック用)"
    (do
      (load-file (str (get-home) "/.grimoire.clj"))
      (load-file (str (get-home) "/Dropbox/program/clojure/grimoire-cli/src/grimoire/commands.clj"))
      (load-file (str (get-home) "/Dropbox/program/clojure/grimoire-cli/src/grimoire/plugin.clj"))
      (load-file (str (get-home) "/Dropbox/program/clojure/grimoire-cli/src/grimoire/gui.clj"))
      (.setRoot (.getScene @main-stage)
        (-> "main.fxml" resource FXMLLoader/load))
      (use 'grimoire.commands)
      (use 'grimoire.gui)
      (use 'grimoire.plugin)))
  ([file]
    (do
      (load-file (str (get-home) "/Dropbox/program/clojure/grimoire-cli/src/grimoire/" file ".clj")))))
