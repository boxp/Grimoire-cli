(ns grimoire.services
  (:use [grimoire.oauth :as oauth]
        [grimoire.listener]
        [grimoire.data]
        [clojure.tools.nrepl.server :only (start-server stop-server)])
  (:import 
    (twitter4j TwitterStreamFactory UserStreamListener)
    (twitter4j.conf ConfigurationContext)
    (twitter4j.conf ConfigurationBuilder)))

(def twitterstream (atom nil))

(defn gen-twitterstream
  [listener]
  (let [confbuilder (doto (ConfigurationBuilder.)
    (.setOAuthConsumerKey (:consumerKey consumers))
    (.setOAuthConsumerSecret (:consumerSecret consumers))
    (.setOAuthAccessToken (:token @tokens))
    (.setOAuthAccessTokenSecret (:tokenSecret @tokens)))
        conf (.build confbuilder)]
    (reset! twitterstream 
        (doto (.getInstance (TwitterStreamFactory. conf))
          (.addListener ^twitter4j.UserStreamListener (listener))))))
  
(defn start 
  []
  "start userstream"
  (.user @twitterstream))

(defn stop 
  []
  "stop userstream"
  (.shutdown @twitterstream))

(defn gen-nrepl!
  [port]
  "start nrepl server"
  (reset! nrepl-server 
    (start-server :port port)))

(defn stop-nrepl
  [server]
  "stop nrepl server"
  (stop-server server))
