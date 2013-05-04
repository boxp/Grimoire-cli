(ns grimoire.oauth
  (:import (twitter4j Status)
           (twitter4j Twitter)
           (twitter4j TwitterFactory)
           (twitter4j TwitterException)
           (twitter4j.auth AccessToken)
           (twitter4j.conf ConfigurationContext)
           (twitter4j.auth OAuthAuthorization)))

(def consumers {:consumerKey "Blnxqqx44rdGTZsBYI4bKw" :consumerSecret "bmQIczed6gbdqkN0V8tV11Carwy2PLj7l2bOIAdcoE"})
(def consumerKey (:consumerKey consumers))
(def consumerSecret (:consumerSecret consumers))

(defn get-tokens [] 
  (let [conf (ConfigurationContext/getInstance) 
        auth (doto 
                (OAuthAuthorization. conf) 
                (.setOAuthConsumer consumerKey,consumerSecret))]
         (do  
           (print "Please access URL and get PINCode:")
           (try 
             (println 
               (.getAuthorizationURL (. auth getOAuthRequestToken)) 
               "\nInput PIN:")
             (catch TwitterException e 
               (println (. e toString))))
         (let [twitterTokens (.getOAuthAccessToken auth (read-line))]
         (spit "tokens.clj" (str "{:token " \" (.getToken twitterTokens) \" " :tokenSecret " \" (.getTokenSecret twitterTokens) \" "}"))))))

(try (def tokens 
       (load-file "tokens.clj"))
     (catch Exception e
       (get-tokens)))
