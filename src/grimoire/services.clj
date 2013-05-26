(ns grimoire.services
  (:use [grimoire.oauth :as oauth]
        [grimoire.listener])
  (:import 
    (twitter4j TwitterStreamFactory)
    (twitter4j.conf ConfigurationContext)
    (twitter4j.conf ConfigurationBuilder)))

(def confbuilder 
  (doto (ConfigurationBuilder.)
    (.setOAuthConsumerKey (:consumerKey consumers))
    (.setOAuthConsumerSecret (:consumerSecret consumers))
    (.setOAuthAccessToken (:token tokens))
    (.setOAuthAccessTokenSecret (:tokenSecret tokens))))

(def conf (.build confbuilder))

(def twitterstream 
    (doto (.getInstance (TwitterStreamFactory. conf))
      (.addListener listener)))

(defn start []
  "start userstream"
  (.user twitterstream))

(defn stop []
  "stop userstream"
  (.shutdown twitterstream))

