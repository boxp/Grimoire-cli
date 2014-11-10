(ns grimoire.core
  (:import (javafx.embed.swing JFXPanel)
           (javax.swing SwingUtilities)
           (javafx.application Application))
  (:require [grimoire.commands :refer [add-runlater]])
  (:gen-class))

(defn- main 
  [& args]
  ; for toolkit initializing
  (javafx.embed.swing.JFXPanel.)
  (SwingUtilities/invokeLater 
    (fn []
      (add-runlater (Application/launch (into-array String args))))))
