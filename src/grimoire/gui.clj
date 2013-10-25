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
  (let [; load fxml layout
        root (-> "main.fxml" io/resource FXMLLoader/load)
        scene (Scene. root 400 600)
        mentions (reverse (.getMentions twitter))]
    (do
      ; backup scene
      (reset! mainscene scene)
      ; theme setting
      (set-theme @theme)
      ; set Icon
      (.. stage getIcons (add (Image. "Grimoire_logo.png" (double 32) (double 32) true true)))
      ; add mentioins tweets
      (add-runlater
        (future
          (do
            ; I couldn't make sence how this code delete. 
            (dosync
              (alter tweets (comp vec concat) mentions))
            (print (map gen-node! mentions))
            (map gen-node! mentions))))
      (doto stage 
        (.setTitle "Grimoire - v0.1.2")
        (.setScene scene)
        .show))))

; javafx start
; dirty
(defn MainApp-start [this ^Stage stage]
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
