(ns grimoire.services
  (:use [grimoire.oauth :as oauth]
        [grimoire.data]
        [clojure.tools.nrepl.server :only (start-server stop-server)])
  (:import (twitter4j Status Twitter TwitterFactory TwitterException TwitterStreamFactory UserStreamListener)
           (twitter4j.auth AccessToken OAuthAuthorization)
           (twitter4j.conf ConfigurationContext ConfigurationBuilder)
           (java.io File)))

(defn gen-twitterstream
  [listener]
  (let [confbuilder (doto (ConfigurationBuilder.)
          (.setOAuthConsumerKey (:consumerKey consumers))
          (.setOAuthConsumerSecret (:consumerSecret consumers))
          (.setOAuthAccessToken (:token @tokens))
          (.setOAuthAccessTokenSecret (:tokenSecret @tokens)))
        conf (.build confbuilder)
        tsi (doto (.getInstance (TwitterStreamFactory. conf))
              (.addListener ^twitter4j.UserStreamListener (listener @twitter nodes mention-nodes)))]
    (reset! twitterstream tsi)
    (dosync 
      (alter twitterstreams merge {(keyword @myname) tsi}))))

(defn token-2-twitterstream
  "トークンのMapからTwitterStreamインスタンスを生成し，返します, token:トークンのマップ, twitter:Twitterインスタンス, nodes-list:NodesListレコード"
  [token twitter nodes-list listener]
  (let [confbuilder (doto (ConfigurationBuilder.)
                      (.setDebugEnabled true)
                      (.setPrettyDebugEnabled true)
                      (.setOAuthConsumerKey (:consumerKey consumers))
                      (.setOAuthConsumerSecret (:consumerSecret consumers))
                      (.setOAuthAccessToken (:token tokens))
                      (.setOAuthAccessTokenSecret (:tokenSecret tokens)))
        conf (.build confbuilder)
        nodes (:nodes nodes-list)
        mentions (:mention-nodes nodes-list)
        tsi (doto (.getInstance (TwitterStreamFactory. conf))
              (.addListener ^twitter4j.UserStreamListener 
                (listener twitter nodes mention-nodes)))]
    tsi))


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
