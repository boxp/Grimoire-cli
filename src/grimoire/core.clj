(ns grimoire.core
  (:import (javafx.embed.swing JFXPanel)
           (javax.swing SwingUtilities)
           (javafx.application Application)
           (javafx.fxml FXML FXMLLoader)
           (javafx.stage Stage)
           (javafx.scene Scene)
           (javafx.scene.image Image))
  (:require [grimoire.oauth :refer [get-oauthtoken! get-tokens gen-twitter]]
            [grimoire.services :refer [gen-twitterstream start]]
            [grimoire.listener :refer [listener]]
            [grimoire.gui :refer [mainwin]]
            [grimoire.data :refer :all]
            [clojure.java.io :as io]))

(gen-class
  :name "App"
  :main true
  :extends javafx.application.Application)

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
          ; set name
          (reset! myname (. @twitter getScreenName))
          (gen-twitterstream listener)
          (start)
          (mainwin stage))
        ; start sign up scene 
        (doto stage
          (.setTitle "Twitter Sign up")
          (.. getIcons (add (Image. "bird_blue_32.png" (double 32) (double 32) true true)))
          (.setScene (Scene. signup 600 400))
          .show)))))

(defn -main 
  [& args]
  (Application/launch (Class/forName "App") (into-array String [])))
