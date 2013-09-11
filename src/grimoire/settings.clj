(ns grimoire.settings
  (:import (java.io File)))

(def settings 
  (atom {:offlinemode false
         :postfailer true}))

(try 
  (reset! settings
    (load-file 
      (str 
        (System/getenv "HOME") 
        "/.grimoire/settings.clj")))
  (catch Exception e 
    (do 
      (def settings
        {:offlinemode false
         :postfailer true})
      (.mkdir (File. (str (System/getenv "HOME") "/.grimoire")))
      (spit 
        (str
          (System/getenv "HOME")
          "/.grimoire/settings.clj")
        settings))))
