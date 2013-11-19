(ns grimoire.gui
  (:import (javafx.scene.input Clipboard ClipboardContent)
           (javafx.application Application Platform)
           (javafx.scene Node Scene)
           (javafx.scene.web WebView)
           (javafx.scene.input KeyCode)
           (javafx.scene.text Text Font FontWeight)
           (javafx.scene.control Label TextField PasswordField Button Hyperlink ListView TabPane Tab ContextMenu MenuItem ListCell)
           (java.lang Runnable)
           (java.io File)
           (javafx.util Callback)
           (javafx.scene.layout GridPane HBox VBox Priority)
           (javafx.scene.paint Color)
           (javafx.scene.image Image ImageView)
           (javafx.geometry Pos Insets)
           (javafx.event EventHandler)
           (javafx.stage Stage Modality Popup)
           (javafx.scene.web WebView)
           (javafx.collections FXCollections ObservableList)
           (javafx.fxml FXML FXMLLoader)
           (twitter4j StatusUpdate))
  (:use [grimoire.oauth]
        [grimoire.services :only [start stop gen-twitterstream]]
        [grimoire.data]
        [grimoire.plugin]
        [grimoire.commands]
        [grimoire.wrapper]
        [grimoire.listener]
        [clojure.string :only [split]])
  (:require [clojure.java.io :as io])
  (:gen-class
   :extends javafx.application.Application))

(defn reply-form!
  "リプライフォームを開き，statusnumに返信します."
  [statusnum]
  (let [target (gen-node! (@tweets statusnum))
        form (TextField. (str "@" (.. (@tweets statusnum) getUser getScreenName) " "))
        rbtn (doto (Button. "@")
               (.setId "Button")
               (.setOnAction 
                 (proxy [EventHandler] []
                   (handle [_]
                     (.updateStatus @twitter
                       (doto 
                         (StatusUpdate.
                           (if (> (count (.getText form)) 140)
                             (apply str (take 137 (.getText form)) "...")
                             (.getText form)))
                         (.inReplyToStatusId (.getId (@tweets statusnum)))))))))
        mainimg (Image. (.. @twitter (showUser @myname) getBiggerProfileImageURL) 20 20 true false true)
        imgview (ImageView. mainimg)
        bottom (doto (HBox.)
                 (.. getChildren (add imgview))
                 (.. getChildren (add form))
                 (.. getChildren (add rbtn))
                 (.setSpacing 20))
        root (doto (VBox.)
               (.. getChildren (add target))
               (.. getChildren (add bottom)))
        scene (doto (Scene. root)
                (.. getStylesheets (add (str @theme ".css"))))
        stage (doto (Stage.)
                (.setScene scene)
                (.setTitle (str "Grimoire - " (.. (@tweets statusnum) getUser getScreenName))))]
    (do
      (HBox/setHgrow bottom Priority/ALWAYS)
      (HBox/setHgrow form Priority/ALWAYS)
      (HBox/setMargin target 
        (Insets. 20 20 0 20))
      (HBox/setMargin bottom 
        (Insets. 0 20 20 20))
      (. stage show))))

(defn show-profile!
  "プロファイルウインドウを生成し，表示します．user:twitter4j.Userインスタンス"
  [user]
  (let [image (doto (ImageView. (. user getBiggerProfileImageURL))
                (.setFitHeight 78)
                (.setFitWidth 78))
        lbl (doto (Label. (. user getScreenName))
              (.setFont (Font. 20)))
        desc (Label. (. user getDescription))
        hl (doto (Hyperlink. (. user getURL))
             (.setOnAction 
               (proxy [EventHandler] []
                 (handle [_]
                   (gen-webview (. user getURL))))))
        vbox (doto (VBox.)
               (.setAlignment Pos/CENTER)
               (.setSpacing 20)
               (.. getChildren (add image))
               (.. getChildren (add lbl))
               (.. getChildren (add desc))
               (.. getChildren (add hl)))
        status (reverse (.getUserTimeline @twitter (. user getId)))
        ol (FXCollections/observableArrayList (to-array []))
        lv (doto (ListView. ol)
            (.setMaxWidth Double/MAX_VALUE)
            (.setMaxHeight Double/MAX_VALUE))
        usertweets (doto (Tab. "Tweets")
                 (.setContent lv)
                 (.setClosable false))
        tabpane (doto (TabPane.)
                  (.. getTabs (add usertweets)))
        root (doto (VBox.)
               (.. getChildren (add vbox))
               (.. getChildren (add tabpane)))
        scene (Scene. root 400 600)
        stage (doto (Stage.)
                (.setTitle (str "Grimoire - @" (. user getScreenName)))
                (.setScene scene))]
      (do
        (VBox/setVgrow tabpane Priority/ALWAYS)
        (HBox/setHgrow lv Priority/ALWAYS)
        (VBox/setVgrow root Priority/ALWAYS)
        (VBox/setMargin image (Insets. 20 0 0 0))
        (add-runlater
          (do
            (dosync
              (alter tweets (comp vec concat) status))
            (doall (map #(add-nodes! ol %) status))))
        (. stage show))))

(defn gen-pane
  "新規ペインを作って返します, image: ペインタイトルに表示するアイコン, lbl: ペインタイトル, 
   coll: ペインのメインタブが表示するObservableList, acount: メインタブが表示するアカウントのtwitterインスタンス"
  [#^java.lang.String image #^java.lang.String title #^ObservableList coll #^twitter4j.Twitter acount]
  (let [lbl (doto (Label. title)
              (.setId "label")
              (.setFont (Font. 20)))
        image (doto (ImageView. image)
                (.setFitHeight 20)
                (.setFitWidth 20))
        hbox (doto (HBox.)
               (.setId "node")
               (.setSpacing 5)
               (.. getChildren (add image))
               (.. getChildren (add lbl)))
        replymenu (MenuItem. "Reply")
        favmenu (MenuItem. "Favorite")
        retmenu (MenuItem. "Retweet")
        favretmenu (MenuItem. "Fav&Retweet")
        conm (doto (ContextMenu.) 
               (.. getItems (add replymenu))
               (.. getItems (add favmenu))
               (.. getItems (add retmenu))
               (.. getItems (add favretmenu)))
        ;cf (reify Callback 
        ;     (call [this _]
        ;       #^ListCell
        ;       (proxy [ListCell] []
        ;         (updateItem [#^twitter4j.Status status #^java.lang.Boolean emp?]
        ;           (proxy-super updateItem status emp?)
        ;           (future
        ;             (add-runlater
        ;               (if (not emp?)
        ;                 (.setGraphic this
        ;                   (gen-node! #^twitter4j.Status status)))))))))
        listv (doto (ListView.)
                (.setId (str title "-tllv"))
                (.setItems coll)
                (.setContextMenu conm))
                ;(.setCellFactory cf))
        img (doto (Image. 
                    (.. acount 
                      (showUser (. acount getScreenName)) getBiggerProfileImageURL)))
        imgv (doto (ImageView. img)
               (.setX 20)
               (.setY 20))
        tab (doto (Tab. (. acount getScreenName))
              (.setContent listv)
              (.setGraphic imgv)
              (.setClosable false))
        tabpane (doto (TabPane.)
                  (.. getTabs (add tab)))
        root (doto (VBox.)
               (.setPrefWidth 100)
               (.setPrefHeight 200)
               (.. getChildren (add hbox))
               (.. getChildren (add tabpane)))]
    (do
      (HBox/setHgrow hbox Priority/ALWAYS)
      (HBox/setHgrow listv Priority/ALWAYS)
      (VBox/setVgrow listv Priority/ALWAYS)
      (HBox/setHgrow root Priority/ALWAYS)
      (VBox/setVgrow tabpane Priority/ALWAYS)
      (.setOnAction replymenu
        (proxy [EventHandler] []
          (handle [_]
            (reply-form! (.indexOf @tweets (focused-status listv)))))) 
      (.setOnAction favmenu
        (proxy [EventHandler] []
          (handle [_]
            (if @nervous
              (bool-dialog 
                (str "Are you sure want to Favorite?\n"
                     (. (focused-status listv) getText))
                     "Sure"
                     #(fav)
                     "Cancel"))
            (fav))))
      (.setOnAction retmenu
        (proxy [EventHandler] []
          (handle [_]
            (if @nervous
              (bool-dialog 
                (str "Are you sure want to Favorite?\n"
                     (. (focused-status listv) getText))
                     "Sure"
                     #(ret)
                     "Cancel")
              (ret)))))
      (.setOnAction favretmenu
        (proxy [EventHandler] []
          (handle [_]
            (if @nervous
              (bool-dialog 
                (str "Are you sure want to Favorite & Retweet?\n"
                     (. (focused-status listv) getText))
                     "Sure"
                     #(favret)
                     "Cancel")
              (favret)))))
      (.setOnContextMenuRequested listv
                  (proxy [EventHandler] [] 
                    (handle [e] 
                      (let [users (cons (. (focused-status) getUser) (vec (map #(.showUser @twitter (. % getScreenName)) (.. (focused-status) getUserMentionEntities))))
                            useritms (map 
                                      #(doto (MenuItem. (str "@" (. % getScreenName)))
                                        (.setOnAction
                                          (proxy [EventHandler] []
                                            (handle [_]
                                              (show-profile! %)))))
                                      users)
                            urls (filter #(= (seq "http") (take 4 %)) (split (.. (focused-status listv) getText) #"\s|\n|　"))
                            urlitms (map 
                                  #(doto (MenuItem. %)
                                    (.setOnAction
                                      (proxy [EventHandler] []
                                        (handle [_]
                                          (gen-webview %)))))
                                  urls)
                            plgs (map 
                                  #(if (. % get-name)
                                     (doto (MenuItem. (. % get-name))
                                       (.setOnAction
                                         (proxy [EventHandler] []
                                           (handle [e]
                                             (.on-click % e))))))
                                   @plugins)]
                        (add-runlater
                          (do
                            (try
                              (.. conm getItems (remove 4 (.. conm getItems size)))
                              (catch Exception e nil))
                            (try
                              (doall
                                (map #(.. conm getItems (add %))
                                  (concat useritms urlitms plgs)))
                              (catch Exception e nil))))))))
      (.setOnKeyPressed root
        (proxy [EventHandler] []
          (handle [ke]
            (try
              ((@key-maps [(.. ke getCode getName) (.isControlDown ke) (.isShiftDown ke)]) listv)
              (catch Exception e (println (.getMessage e)))))))                 
      root)))


; main window
; dirty
(defn mainwin
  [^Stage stage]
  (let [; load fxml layout
        root (-> "main.fxml" io/resource FXMLLoader/load)
        scene (Scene. root 800 600)
        mentions (reverse (.getMentions @twitter))]
    (do
      ; backup scene
      (reset! mainscene scene)
      ; Add master pane
      (.. scene (lookup "#pane") getChildren (add 
        (gen-pane "home.png" "HomeTimeline" nodes @twitter)))
      ; Add mentions pane
      (.. scene (lookup "#pane") getChildren (add 
        (gen-pane "reply_hover.png" "Mentions" mention-nodes @twitter)))
      ; check update
      (check-update)
      ; load rcfile
      (future
        (binding [*ns* (find-ns 'grimoire.gui)]
          (try (load-file 
                 (str (get-home)
                   "/.grimoire.clj"))
            (catch Exception e (println e)))))
      ; サブアカウントを読み込み
      (dosync
        (alter subtokens merge 
          (try
            (load-file (str (get-home) "/.grimoire/subtokens.clj"))
            (catch Exception e {}))))
      ; set name
      (reset! myname (. @twitter getScreenName))
      ; set main acount image
      (refresh-profileimg!)
      ; add mentioins tweets
      (dosync
        (alter tweets (comp vec concat) mentions))
      (doall
        (map #(add-nodes! nodes %) mentions))
      (doall
        (map #(add-nodes! mention-nodes %) mentions))
      (doto stage 
        (.setTitle "Grimoire - v0.1.2")
        (.setScene scene)
        .show)
      ; theme setting
      (set-theme! @theme)
      (try
        (doall
          (map #(.on-start %)
            @plugins))
        (catch Exception e (print-node! (.getMessage e)))))))

; javafx start
; dirty
(defn -start [this ^Stage stage]
  (let [signup (-> "signin.fxml" io/resource FXMLLoader/load)]
    (do
      (get-oauthtoken!)
      (reset! main-stage stage)
      (if (get-tokens)
        (do 
          ; send stage to fxml
          (get-tokens)
          (gen-twitter)
          (gen-twitterstream listener)
          (start)
          (mainwin stage))
        ; start sign up scene 
        (doto stage
          (.setTitle "Twitter Sign up")
          (.. getIcons (add (Image. "bird_blue_32.png" (double 32) (double 32) true true)))
          (.setScene (Scene. signup 600 400))
          .show)))))
