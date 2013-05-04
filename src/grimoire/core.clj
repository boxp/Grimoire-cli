(ns grimoire.core
  (:require [grimoire.oauth :as oauth])
  (:import (twitter4j TwitterFactory)
           (twitter4j.auth AccessToken))
  (:gen-class))

(defn -main []
  (do

    (println "Grimoire has started v0.0.2")
    (println "          _ ........_")
    (println "          , ´,.-==-.ヽ")
    (println "            ((ﾉﾉ))ﾉ）)")
    (println "          ハ) ﾟ ヮﾟﾉ)       Welcome!")
    (println "          ~,く__,ネﾉ)つ")
    (println "          |(ﾝ_l|,_,_ﾊ、")
    (println "          ｀~ し'.ﾌ~´")

(def consumers {:consumerKey "Blnxqqx44rdGTZsBYI4bKw" :consumerSecret "bmQIczed6gbdqkN0V8tV11Carwy2PLj7l2bOIAdcoE"})
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
    (try (str "Success:" (.getText (.updateStatus twitter input)))
             (catch Exception e (println "Something has wrong."))))

  (defn showtl []
    (let [statusAll (reverse (.getHomeTimeline twitter))]
      (loop [status statusAll i 1]
        (if (= i 20)
          nil
          (do
            (println (.getScreenName (.getUser (first status))) ":" (.getText (first status)))
            (recur (rest status) (+ i 1)))))))

  (defn reload []
    (load-file "src/grimoire/core_commands.clj"))

    (loop [input (read-line)]
      (if (= "exit" input)
          (try (println "bye bye!")
               (catch Exception e (println e)))
          (do (print "Grimoire => ")
              (try (println (load-string (str "(in-ns `grimoire.core) " input)))
                   (catch Exception e (println e)))
              (recur (read-line)))))))
