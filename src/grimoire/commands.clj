(ns grimoire.commands
  (:use [clojure.repl :as repl]
        [grimoire.oauth :as oauth]
        [grimoire.services]
        [grimoire.listener])
  (:import (twitter4j TwitterFactory)
           (twitter4j.auth AccessToken)
           (twitter4j StatusUpdate)))

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
  [& input]
  (try 
    (str "Success:" 
      (.getText 
        (.updateStatus twitter 
          (if 
            (> (count (seq (apply str input))) 140)
              (str (apply str (take 137 (seq (apply str input)))) "...")
              (apply str input)))))
    (catch Exception e (println "Something has wrong." e))))

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
           "start: start userstream.\n"
           "stop: stop userstream.\n"
           "fav: Favorite status.\n"
           "retweet: Retweet status.\n"
           "favret: Retweet and Favorite status.\n"
           "reply: reply to tweet"
           "Get more information to (doc <commands>)."))

(defn start []
  "start userstream"
  (.user twitterstream))

(defn stop []
  "stop userstream"
  (.shutdown twitterstream))

(defn retweet 
  "Retweet Timeline's status number."
  [statusnum]
  (try 
    (let [status (.retweetStatus twitter (:id (tweets statusnum)))]
      (str 
        "Success retweet: @" 
        (.. status getUser getScreenName)
        " - "
        (.. status getText)))
    (catch Exception e "something has wrong.")
    ))

(defn fav
  "Favorite Timeline's status number."
  [statusnum]
  (try
    (let [status (.createFavorite twitter (:id (tweets statusnum)))]
      (str
        "Success Fav: @" 
        (.. status getUser getScreenName)
        " - "
        (.. status getText)))
    (catch Exception e "something has wrong.")))

(defn favret [statusnum]
  "Favorite and Retweet Timeline's status number"
  (do 
    (fav statusnum)
    (retweet statusnum)))

(defn reply [statusnum & texts]
  "Reply to tweets"
    (.updateStatus 
      twitter 
      (doto
        (StatusUpdate. 
          (str
            \@
            (:user
              (tweets statusnum))
            " "
            (apply str texts)))
        (.setInReplyToStatusId
          (:id 
            (tweets statusnum))))))

; デバック用
(defn reload []
  (load-file "src/grimoire/commands.clj"))
