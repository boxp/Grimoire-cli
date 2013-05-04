(ns grimoire.commands
  (:use [clojure.repl :as repl]
        [grimoire.oauth :as oauth])
  (:import (twitter4j TwitterFactory)
           (twitter4j.auth AccessToken)))

(try 
  (def twitter (doto (.getInstance (TwitterFactory.))
    (.setOAuthConsumer consumerKey,consumerSecret)
    (.setOAuthAccessToken 
      (AccessToken. 
        (:token tokens) 
        (:tokenSecret tokens)))))
  (catch Exception e (println e)))


(defn post 
  " Post tweets \nUsage: (post \"hoge\")"
  [input & more]
  (try 
    (str "Success:" 
      (.getText 
        (.updateStatus twitter 
          (str input
               (apply str more)))))
    (catch Exception e (println "Something has wrong."))))

(defn showtl 
  " Showing 20 new tweets from HomeTimeline.\nUsage: (showtl)"
  []
  (let [statusAll (reverse (.getHomeTimeline twitter))]
    (loop [status statusAll i 1]
      (if (= i 20)
        nil
        (do
          (println (.getScreenName (.getUser (first status))) ":" (.getText (first status)))
          (recur (rest status) (+ i 1)))))))

(defn help []
  (str     "\n"
           "*** Grimoire-cli Commands List***\n"
           "post: Post tweets\n"
           "showtl: Showing 20 new Tweets from HomeTimeline.\n"
           "Get more information to (doc <commands>)."))

; デバック用
(defn reload []
  (load-file "src/grimoire/commands.clj"))
