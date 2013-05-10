(ns grimoire.oauth
  (:use [clojure.java.browse])
  (:import (twitter4j Status)
           (twitter4j Twitter)
           (twitter4j TwitterFactory)
           (twitter4j TwitterException)
           (twitter4j.auth AccessToken)
           (twitter4j.conf ConfigurationContext)
           (twitter4j.auth OAuthAuthorization)
           (java.io File)))

(def consumers {:consumerKey "Blnxqqx44rdGTZsBYI4bKw" :consumerSecret "bmQIczed6gbdqkN0V8tV11Carwy2PLj7l2bOIAdcoE"})
(def consumerKey (:consumerKey consumers))
(def consumerSecret (:consumerSecret consumers))

(defn get-tokens [] 
  (let [conf (ConfigurationContext/getInstance) 
        auth (doto 
                (OAuthAuthorization. conf) 
                (.setOAuthConsumer consumerKey,consumerSecret))]
         (do  
           (try 
             (do
               (try 
                 (browse-url (.getAuthorizationURL 
                   (. auth getOAuthRequestToken)))
                 (catch Exception e nil))
               (println
                 "Please access URL and get PIN:"
                 (.getAuthorizationURL 
                 (. auth getOAuthRequestToken)) 
                 "\nInput PIN:"))
               (catch TwitterException e 
                 (println 
                  (. e toString))))
             (.mkdir (File. (str (System/getenv "HOME") "/.grimoire")))
           (let 
            [twitterTokens 
              (.getOAuthAccessToken auth (read-line))]
             (do
               (def tokens 
                 {:token (.getToken twitterTokens) 
                  :tokenSecret (.getTokenSecret twitterTokens)})
               (spit 
                 (str 
                   (System/getenv "HOME") 
                   "/.grimoire/tokens.clj") 
                 (str tokens)))))))

(try 
  (def tokens 
    (load-file (str (System/getenv "HOME") "/.grimoire/tokens.clj")))
  (catch Exception e
    (get-tokens)))
