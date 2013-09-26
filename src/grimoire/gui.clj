(ns grimoire.gui
  (:import (javafx.scene.input Clipboard ClipboardContent)
           (javafx.application Application Platform)
           (javafx.scene Node Scene)
           (javafx.scene.web WebView)
           (javafx.scene.input KeyCode)
           (javafx.scene.text Text Font FontWeight)
           (javafx.scene.control Label TextField PasswordField Button Hyperlink ListView)
           (javafx.scene.layout GridPane HBox VBox Priority)
           (javafx.scene.paint Color)
           (javafx.scene.image Image ImageView)
           (javafx.geometry Pos Insets)
           (javafx.event EventHandler)
           (javafx.stage Stage Modality)
           (javafx.scene.web WebView)
           (javafx.collections FXCollections ObservableList)
           (javafx.fxml FXML FXMLLoader))
  (:use [grimoire.oauth :only [get-tokens gen-tokens oauthtoken gen-twitter]]
        [grimoire.services :only [start stop gen-twitterstream]]
        [grimoire.datas]
        [grimoire.commands]
        [clojure.java.browse]
        [grimoire.listener])
  (:require [clojure.java.io :as io])
  (:gen-class
   :prefix MainApp-
   :name MainApp
   :extends javafx.application.Application))

; main window
; dirty
(defn mainwin
  [^Stage stage]
  (let [maintlt (Label. "HomeTimeLine")
        client (doto (VBox.)
                 (.setPadding (Insets. 5))
                 (.setSpacing 5))
        column (HBox.)
        pressed (atom false)
        maintl (doto (VBox.)
                 (.setId "maintl"))
        form (doto (TextField.)
               (.setPrefWidth 2000)
               (.setOnAction (proxy [EventHandler] []
                               (handle [_]
                                 nil))))
        post (doto (Button. "æŠ•ç¨¿")
               (.setMinWidth 60)
               (.setOnAction (proxy [EventHandler] []
                               (handle [_]
                                 (post (.getText form))))))
        futter (doto (HBox.)
                 (.. getChildren (add form))
                 (.. getChildren (add post))
                 (.setSpacing 5))
        spell (doto (Button. "Î»")
               (.setMinWidth 60)
               (.setOnAction (proxy [EventHandler] []
                               (handle [_]
                                 (do
                                   (.. futter getChildren (remove 1 2))
                                   (.. futter getChildren (add post))
                                   (binding [*ns* (find-ns 'grimoire.core)]
                                     (.add @nodes 0 (try 
                                                     (load-string (.getText form))
                                                     (catch Exception e e)))))))))
        scene (Scene. client 530 840)
        listv (doto (ListView. @nodes)
                (.setPrefWidth 520)
                (.setId "listv"))]
    (do
      (.. scene getStylesheets (add "solarized_dark.css"))
      (VBox/setVgrow client Priority/SOMETIMES)
      (HBox/setHgrow maintl Priority/SOMETIMES)
      (VBox/setVgrow column Priority/SOMETIMES)
      (VBox/setVgrow listv Priority/SOMETIMES)
      (HBox/setHgrow listv Priority/SOMETIMES)
      (HBox/setHgrow form Priority/SOMETIMES)
      (HBox/setHgrow maintlt Priority/ALWAYS)
      (.setOnKeyPressed form (proxy [EventHandler] []
                               (handle [ke]
                                 (cond
                                   (and (= (.getCode ke) (KeyCode/ENTER)) (.isControlDown ke)) (.. futter getChildren (.get 2) fire)))))
      (.setOnKeyPressed listv (proxy [EventHandler] []
                                    (handle [ke]
                                      (let [idx (.. listv getSelectionModel getSelectedIndex)]
                                        (cond 
                                          (= (.getText ke) "(") (do 
                                                                  (.. futter getChildren (remove 1 2))
                                                                  (.. futter getChildren (add spell))
                                                                  (.requestFocus form))
                                          (= (.getText ke) "j") (do 
                                                                  (.. listv getSelectionModel (select (inc (.. listv getSelectionModel getSelectedIndex))))
                                                                  (.. listv (scrollTo (inc (.. listv getSelectionModel getSelectedIndex)))))
                                          (= (.getText ke) "k") (do 
                                                                  (.. listv getSelectionModel (select (dec (.. listv getSelectionModel getSelectedIndex))))
                                                                  (.. listv (scrollTo (dec (.. listv getSelectionModel getSelectedIndex)))))
                                          (= (.getText ke) "g") (do 
                                                                  (.. listv getSelectionModel (select 0))
                                                                  (.. listv (scrollTo 0)))
                                          (= (.getText ke) "G") (do
                                                                  (.. listv getSelectionModel (select (dec (.size @nodes))))
                                                                  (.. listv (scrollTo (dec (.size @nodes)))))
                                          :else nil)))))
      (doto (.. maintl getChildren)
        (.add maintlt)
        (.add listv))
      (doto (.. column getChildren)
        (.add maintl))
      (doto (.. client getChildren)
        (.add column)
        (.add futter))
      (doto stage 
        (.setTitle "Grimoire - v0.1.2")
        (.setScene scene)
        .show))))
  

; javafx‹N“®
; dirty
(defn MainApp-start [this ^Stage stage]
  (let [signup (-> "signin.fxml" io/resource FXMLLoader/load)]
      (try 
        (do 
          (get-tokens)
          (gen-twitter)
          (gen-twitterstream)
          (start)
          (mainwin stage))
        (catch Exception e 
          (doto stage
            (.setTitle "Twitter Sign up")
            (.setScene (Scene. signup 600 400))
            .show)))))
