(ns grimoire.oauth
  (:use [grimoire.data])
  (:import (twitter4j Status Twitter TwitterFactory TwitterException)
           (twitter4j.auth AccessToken OAuthAuthorization)
           (twitter4j.conf ConfigurationContext)
           (java.io File)))

(def tokens (atom nil))
(def consumers {:consumerKey "Blnxqqx44rdGTZsBYI4bKw" :consumerSecret "bmQIczed6gbdqkN0V8tV11Carwy2PLj7l2bOIAdcoE"})
(def consumerKey (:consumerKey consumers))
(def consumerSecret (:consumerSecret consumers))
(defn get-oauthtoken!
  []
  (let [conf (ConfigurationContext/getInstance) 
        auth (doto (OAuthAuthorization. conf) 
                (.setOAuthConsumer consumerKey,consumerSecret))]
    (reset! oauthtoken
      (.. auth getOAuthRequestToken))))

(defn gen-tokens [pin] 
  (let [conf (ConfigurationContext/getInstance) 
        auth (doto (OAuthAuthorization. conf) 
                (.setOAuthConsumer consumerKey,consumerSecret))]
       (do  
         (.mkdir (File. (str (System/getenv "HOME") "/.grimoire")))
         (let 
           [twitterTokens 
             (.getOAuthAccessToken auth @oauthtoken pin)]
           (do
             (reset! tokens 
               {:token (.getToken twitterTokens) 
                :tokenSecret (.getTokenSecret twitterTokens)})
             (spit 
               (str 
                 (System/getenv "HOME") 
                 "/.grimoire/tokens.clj") 
               (str @tokens)))))))

(defn get-tokens []
    (try
      (reset! tokens 
        (load-file (str (System/getenv "HOME") "/.grimoire/tokens.clj")))
      (catch Exception e nil)))

(defn gen-twitter []
  (try 
    (def twitter (doto (.getInstance (TwitterFactory.))
      (.setOAuthConsumer consumerKey,consumerSecret)
      (.setOAuthAccessToken 
        (AccessToken. 
          (:token @tokens) 
          (:tokenSecret @tokens)))))
    (catch Exception e (println e))))
