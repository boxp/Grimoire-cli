(ns grimoire.core
  (:use [clojure.repl]
        [grimoire.oauth :as oauth]
        [grimoire.commands]
        [grimoire.services]
        [grimoire.listener :as listener]
        [grimoire.settings]
        [grimoire.login-form])
  (:import (javafx.application Application)
           Login)
  (:gen-class))

; 起動時に呼ばれる
; dirty
(defn -main []
  (do

    ; twitterにログイン
    (try (get-tokens) 
      (catch Exception e (Application/launch Login (into-array String [])))) 
    (gen-twitter)
    (gen-twitterstream)

    ; タイトル
    (print
      "Grimoire has started v0.0.7\n"
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

    ; lanterna repl

;     (loop [input (t/get-key-blocking term)]
;       (case input
;           \q (do 
;                 (t/clear term)
;                 (pop-window "bye bye!" "quiting...")
;                 (.shutdown twitterstream))
;           :enter (newbuffer nil)
;           (recur (t/get-key-blocking term))))))


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
            (.shutdown twitterstream))
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
            (recur (read-line)))))))
