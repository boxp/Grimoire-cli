(ns grimoire.core
  (:use [clojure.repl]
        [grimoire.oauth :as oauth]
        [grimoire.commands]
        [grimoire.services]
        [grimoire.listener]
        [grimoire.gui]
        [grimoire.datas])
  (:import (javafx.application Application)
           (java.io File)
           MainApp)
  (:gen-class))


; 起動時に呼ばれる
; dirty
(defn -main
  ([] (do
        ; pluginのロード
        ; dirty
        (let [file (File.  (str get-home "/.grimoire/plugin"))] 
          (map load-file (. file list)))
        ; load rcfile
        (binding [*ns* (find-ns 'grimoire.core)]
          (try (load-file 
                 (str (get-home)
                   "/.grimoire.clj"))
            (catch Exception e nil)))
        ; javafx向け
        (Application/launch MainApp (into-array String []))
        (spit 
           (str 
             (get-home) 
             "/.grimoire/cache.clj")
              (vec (take 100 @tweets)))
        (.shutdown @twitterstream)))
  ([& args]
    (do
      ; コンソール向け
      (try 
        (do 
          (get-tokens)
          (gen-twitter)
          (gen-twitterstream c-listener)
          (start))
        (catch Exception e 
          (do
            (println "Please get Pin code")
            (println (.getAuthorizationURL oauthtoken))
            (print "PIN:")
            (flush)
            (gen-tokens (read-line))
            (get-tokens)
            (gen-twitter)
            (gen-twitterstream c-listener)
            (start))))

      ; タイトル
      (print
        "Grimoire has started v0.1.2\n"
        "_ ........_\n"
        ", ´,.-==-.ヽ\n"
        "l ((ﾉﾉ))ﾉ）)\n"
        "ハ) ﾟ ヮﾟﾉ)\n"
        "~,く__,ネﾉ)つ\n"
        "|(ﾝ_l|,_,_ﾊ、\n"
        "｀~ し'.ﾌ~´\n"

       (try
         (str "Welcome " (.getScreenName twitter) "!\n")
         (catch Exception e nil))
       "---------------------------\n"
       "* Grimoire user guide     *\n"
       "---------------------------\n"
       "* Help  : (help)          *\n"
       "* Exit  :  exit           *\n"
       "* Stream: (start)         *\n"
       "---------------------------\n")

     ; REPL
     ; dirty
      (print "Grimoire => ")
      (flush)
      (loop [input (read-line)]
        (cond 
          (= "exit" input)
            (do 
              (try (println "bye bye!")
                (catch Exception e (println e)))
              (.shutdown @twitterstream))
          :else  
            (do 
              (try (let [result (binding [*ns* (find-ns 'grimoire.core)] (load-string input))]
                     (if result
                       (println result)
                       nil))
                   (catch Exception e 
                     (do 
                       (println e))))
              (print "Grimoire => ")
              (flush)
              (recur (read-line))))))))
