(ns grimoire.gui
  (:import (javafx.scene.input Clipboard ClipboardContent)
           (javafx.application Application Platform)
           (javafx.scene Node Scene)
           (javafx.scene.web WebView)
           (javafx.scene.input KeyCode)
           (javafx.scene.text Text Font FontWeight)
           (javafx.scene.control Label TextField PasswordField Button Hyperlink ListView)
           (java.lang Runnable)
           (javafx.scene.layout GridPane HBox VBox Priority)
           (javafx.scene.paint Color)
           (javafx.scene.image Image ImageView)
           (javafx.geometry Pos Insets)
           (javafx.event EventHandler)
           (javafx.stage Stage Modality Popup)
           (javafx.scene.web WebView)
           (javafx.collections FXCollections ObservableList)
           (javafx.fxml FXML FXMLLoader))
  (:use [grimoire.oauth :only [twitter get-tokens gen-tokens oauthtoken gen-twitter]]
        [grimoire.services :only [start stop gen-twitterstream]]
        [grimoire.data]
        [grimoire.plugin]
        [grimoire.commands]
        [clojure.java.browse]
        [grimoire.listener])
  (:require [clojure.java.io :as io])
  (:gen-class
   :extends javafx.application.Application))

; main window
; dirty
(defn mainwin
  [^Stage stage]
  (let [; load fxml layout
        root (try
               (-> "main.fxml" io/resource FXMLLoader/load)
               (catch Exception e (assert (.getMessage e))))
        scene (Scene. root 400 600)
        mentions (reverse (.getMentions twitter))]
    (do
      ; load rcfile
      (binding [*ns* (find-ns 'grimoire.gui)]
        (try (load-file 
               (str (get-home)
                 "/.grimoire.clj"))
          (catch Exception e (println e))))
      ; backup scene
      (reset! mainscene scene)
      ; theme setting
      (set-theme @theme)
      ; add mentioins tweets
      (dosync
        (alter tweets (comp vec concat) mentions))
      (future
        (doall
          (map gen-node! mentions)))
      (doto stage 
        (.setTitle "Grimoire - v0.1.2")
        (.setScene scene)
        .show))))

; javafx start
; dirty
(defn -start [this ^Stage stage]
  (let [signup (-> "signin.fxml" io/resource FXMLLoader/load)]
      (try 
        (do 
          ; send stage to fxml
          (reset! main-stage stage)
          (get-tokens)
          (gen-twitter)
          (gen-twitterstream listener)
          (start)
          (mainwin stage))
        (catch Exception e 
          (do
            ; start sign up scene 
            (doto stage
              (.setTitle "Twitter Sign up")
              (.. getIcons (add (Image. "bird_blue_32.png" (double 32) (double 32) true true)))
              (.setScene (Scene. signup 600 400))
              .show))))))
