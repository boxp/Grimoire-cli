(ns grimoire.core
  (:gen-class)
  (:require [grimoire.oauth :as oauth])
  (:import (twitter4j TwitterFactory)
           (twitter4j.auth AccessToken)))

(defn -main []
  (do

    (println "Grimoire has started v0.0.1")
    (println "          _ ........_")
    (println "          , ´,.-==-.ヽ")
    (println "            ((ﾉﾉ))ﾉ）)")
    (println "          ハ) ﾟ ヮﾟﾉ)       Welcome!")
    (println "          ~,く__,ネﾉ)つ")
    (println "          |(ﾝ_l|,_,_ﾊ、")
    (println "          ｀~ し'.ﾌ~´")

    (def consumers (load-file "keys.clj"))
    (def consumerKey (:consumerKey consumers))
    (def consumerSecret (:consumerSecret consumers))

    (try (def tokens (load-file "tokens.clj"))
         (catch Exception e
           (oauth/get-tokens)))

    (def twitter (doto (.getInstance (TwitterFactory.))
      (.setOAuthConsumer consumerKey,consumerSecret)
      (.setOAuthAccessToken (AccessToken. (:token tokens) (:tokenSecret tokens)))
    ))

    (load-file "src/grimoire/core_commands.clj")

    (loop [input (read-line)]
      (if (= "exit" input)
          (try (println "bye bye!")
               (catch Exception e (println e)))
          (do (print "Grimoire => ")
              (try (println (load-string (str "(in-ns `grimoire.core) " input)))
                   (catch Exception e (println e)))
              (recur (read-line)))))))
