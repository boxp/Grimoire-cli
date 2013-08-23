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
(def oauthurl
  (let [conf (ConfigurationContext/getInstance) 
        auth (doto (OAuthAuthorization. conf) 
                (.setOAuthConsumer consumerKey,consumerSecret))]
    (.. auth getOAuthRequestToken getAuthorizationURL)))

(defn gen-tokens [pin] 
  (let [conf (ConfigurationContext/getInstance) 
        auth (doto (OAuthAuthorization. conf) 
                (.setOAuthConsumer consumerKey,consumerSecret))]
         (do  
           (.mkdir (File. (str (System/getenv "HOME") "/.grimoire")))
           (let 
             [twitterTokens 
               (.getOAuthAccessToken auth pin)]
             (do
               (def tokens 
                 {:token (.getToken twitterTokens) 
                  :tokenSecret (.getTokenSecret twitterTokens)})
               (spit 
                 (str 
                   (System/getenv "HOME") 
                   "/.grimoire/tokens.clj") 
                 (str tokens)))))))

(defn get-tokens []
    (def tokens 
      (load-file (str (System/getenv "HOME") "/.grimoire/tokens.clj"))))

(defn gen-twitter []
  (try 
    (def twitter (doto (.getInstance (TwitterFactory.))
      (.setOAuthConsumer consumerKey,consumerSecret)
      (.setOAuthAccessToken 
        (AccessToken. 
          (:token tokens) 
          (:tokenSecret tokens)))))
    (catch Exception e (println e))))
