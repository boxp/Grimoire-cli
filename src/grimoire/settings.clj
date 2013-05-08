(ns grimoire.settings)

(try 
  (def settings
    (load-file 
      (str 
        (System/getenv "HOME") 
        "/.grimoire/settings.clj")))
  (catch Exception e 
    (do 
      (def settings
        {:offlinemode false
         :postfailer true})
      (spit 
        (str
          (System/getenv "HOME")
          "/.grimoire/settings.clj")
        settings))))
