(ns grimoire.core
  (:require [grimoire.oauth :as oauth])
  (:import (twitter4j TwitterFactory)
           (twitter4j.auth AccessToken)))

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

(defn post [input]
  (do (try (.updateStatus twitter input)
           (catch Exception e (println "Something has wrong.")))))

(defn tweet []
  (.updateStatus twitter (read-line)))

(loop [input (read-line)]
  (if (= "exit" input)
      (try (println "bye bye!")
           (catch Exception e (println e)))
      (do (print "=>")
          (try (println (load-string input))
               (catch Exception e (println e)))
          (recur (read-line)))))
