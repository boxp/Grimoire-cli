(ns grimoire.core
  (:use [clojure.repl]
        [grimoire.oauth :as oauth]
        [grimoire.commands]
        [grimoire.services :as services]
        [grimoire.listener :as listener]
  (:gen-class)))

(defn -main []
  (do

    (println "Grimoire has started v0.0.5")
    (println "          _ ........_")
    (println "          , ´,.-==-.ヽ")
    (println "            ((ﾉﾉ))ﾉ）)")
    (println "          ハ) ﾟ ヮﾟﾉ)       Welcome!")
    (println "          ~,く__,ネﾉ)つ")
    (println "          |(ﾝ_l|,_,_ﾊ、")
    (println "          ｀~ し'.ﾌ~´")
    (println "                          ")
    (println "---------------------------")
    (println "* Usage : (commands)      *")
    (println "* Help  : (help)          *")
    (println "* Exit  :  exit            *")
    (println "* Stream: (start)         *")
    (println "---------------------------")

    (loop [input (read-line)]
      (cond 
        (= "exit" input)
          (do 
            (try (println "bye bye!")
              (catch Exception e (println e)))
            (.shutdown twitterstream))
        :else  
          (do 
            (print "Grimoire => ")
            (try (println (load-string (str "(in-ns `grimoire.core) " input)))
                 (catch Exception e (println e)))
            (recur (read-line)))))))
