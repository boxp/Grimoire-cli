(ns grimoire.services
  (:use [grimoire.oauth :as oauth]
        [grimoire.listener])
  (:import 
    (twitter4j TwitterStreamFactory)
    (twitter4j.conf ConfigurationContext)
    (twitter4j.conf ConfigurationBuilder)))

(def twitterstream 
  (let [confbuilder (doto (ConfigurationBuilder.)
                        (.setOAuthConsumerKey (:consumerKey consumers))
                        (.setOAuthConsumerSecret (:consumerSecret consumers))
                        (.setOAuthAccessToken (:token tokens))
                        (.setOAuthAccessTokenSecret (:tokenSecret tokens)))
        conf (.build confbuilder)]
    (doto 
      (.getInstance 
        (TwitterStreamFactory. conf))
      (.addListener listener)
      (.user))))
