(ns grimoire.core
  (:use [clojure.repl]
        [grimoire.oauth :as oauth]
        [grimoire.seesaw]
        [grimoire.commands]
        [grimoire.services]
        [grimoire.listener :as listener]
        [grimoire.settings])
  (:gen-class))

(defn -main []
  (do

    (println "Grimoire has started v0.0.5\n"
             "          _ ........_\n"
             "          , ´,.-==-.ヽ\n"
             "            ((ﾉﾉ))ﾉ）)\n"
             (try
               (str "          ハ) ﾟ ヮﾟﾉ)       Welcome " (.getScreenName twitter) "!\n")
               (catch Exception e nil))
             "          ~,く__,ネﾉ)つ\n"
             "          |(ﾝ_l|,_,_ﾊ、\n"
             "          ｀~ し'.ﾌ~´\n"
             "                          \n"
             "---------------------------\n"
             "* Usage : (commands)      *\n"
             "---------------------------\n"
             "* Help  : (help)          *\n"
             "* Exit  :  exit           *\n"
             "* Stream: (start)         *\n"
             "---------------------------\n")

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
            (try (println (load-string (str "(in-ns `grimoire.core) " input)))
                 (catch Exception e 
                   (do 
                     (println e))))
            (print "Grimoire => ")
            (flush)
            (recur (read-line)))))))
