(ns grimoire.oauth
  (:use [grimoire.data])
  (:import (twitter4j Status Twitter TwitterFactory TwitterException)
           (twitter4j.auth AccessToken OAuthAuthorization)
           (twitter4j.conf ConfigurationContext)
           (java.io File)))

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
         (.mkdir (File. 
                   (str 
                     (let [home (System/getenv "HOME")]
                       (if home
                         home
                         (System/getProperty "user.home"))) 
                     "/.grimoire")))
         (let 
           [twitterTokens 
             (.getOAuthAccessToken auth @oauthtoken pin)]
           (do
             (reset! tokens 
               {:token (.getToken twitterTokens) 
                :tokenSecret (.getTokenSecret twitterTokens)})
             (spit 
               (str 
                 (let [home (System/getenv "HOME")]
                   (if home
                     home
                     (System/getProperty "user.home"))) 
                 "/.grimoire/tokens.clj") 
               (str @tokens)))))))

(defn get-tokens []
    (try
      (reset! tokens 
        (load-file 
          (str 
            (let [home (System/getenv "HOME")]
              (if home
                home
                (System/getProperty "user.home"))) 
            "/.grimoire/tokens.clj")))
      (catch Exception e nil)))

(defn gen-twitter []
  (let [twitterins (try 
                     (doto (.getInstance (TwitterFactory.))
                       (.setOAuthConsumer consumerKey,consumerSecret)
                       (.setOAuthAccessToken 
                         (AccessToken. 
                           (:token @tokens) 
                           (:tokenSecret @tokens))))
                     (catch Exception e (println e)))
        screen-name-key (keyword (. twitterins getScreenName))]
    (do
      (reset! twitter twitterins)
      (dosync 
        (alter twitters merge
          {screen-name-key twitterins})))))

(defn token-2-twitter
  "token-mapからtwitterインスタンスを生成して返します"
  [token-map]
  (doto (.getInstance (TwitterFactory.))
      (.setOAuthConsumer consumerKey,consumerSecret)
      (.setOAuthAccessToken 
        (AccessToken. 
          (:token token-map) 
          (:tokenSecret token-map)))))

(defn pin-2-token
  "oauthtokenからpinを使って:token,:tokenSecretを持つtokenmapを生成し，返す."
  [pin]
  (let [conf (ConfigurationContext/getInstance) 
        auth (doto (OAuthAuthorization. conf) 
                (.setOAuthConsumer consumerKey,consumerSecret))
        twittertoken (.getOAuthAccessToken auth @oauthtoken (str pin))
        tokenmap {:token (. twittertoken getToken)
                  :tokenSecret (. twittertoken getTokenSecret)}]
     tokenmap))
