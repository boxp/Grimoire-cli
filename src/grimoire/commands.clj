(ns grimoire.commands
  (:use [clojure.repl :as repl]
        [dollswar.math]
        [grimoire.datas]
        [grimoire.oauth :as oauth])
  (:import (twitter4j TwitterFactory)
           (twitter4j.auth AccessToken)
           (twitter4j StatusUpdate)
           (java.io File)))

; pluginのロード
; dirty
(let [file (File. (str (System/getenv "HOME") "/.grimoire/plugin"))] 
  (map load-file (. file list)))

; コマンドたち
; ツイート
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

; 20件のツイート取得
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

; コマンド一覧
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

; リツイート
(defn ret
  "Retweet Timeline's status number."
  [statusnum]
  (try 
    (let [status (.retweetStatus twitter (:id (@tweets statusnum)))]
      (str 
        "Success retweet: @" 
        (.. status getUser getScreenName)
        " - "
        (.. status getText)))
    (catch Exception e "something has wrong.")))

; リツイートの取り消し
(defn unret
  "UnRetweet Timeline's status number."
  [statusnum]
  (try 
    (let [status (.destroyStatus twitter (:id (@tweets statusnum)))]
      (str 
        "Success unretweet: @" 
        (.. status getUser getScreenName)
        " - "
        (.. status getText)))
    (catch Exception e "something has wrong.")))

; ふぁぼふぁぼ
(defn fav
  "Favorite Timeline's status number."
  [statusnum]
  (try
    (let [status (.createFavorite twitter (:id (@tweets statusnum)))]
      (str
        "Success Fav: @" 
        (.. status getUser getScreenName)
        " - "
        (.. status getText)))
    (catch Exception e "something has wrong.")))

; あんふぁぼ
(defn unfav
  "Favorite Timeline's status number."
  [statusnum]
  (try
    (let [status (.destroyFavorite twitter (:id (@tweets statusnum)))]
      (str
        "Success UnFav: @" 
        (.. status getUser getScreenName)
        " - "
        (.. status getText)))
    (catch Exception e "something has wrong.")))

; ふぁぼRT
; clean
(defn favret [statusnum]
  "Favorite and Retweet Timeline's status number"
  (do 
    (fav statusnum)
    (ret statusnum)))

; ふぁぼRT
; clean
(defn unfavret [statusnum]
  "UnFavorite and UnRetweet Timeline's status number"
  (do 
    (unfav statusnum)
    (unret statusnum)))

; つい消し
(defn del
  "Delete Timeline's status number."
  [statusnum]
  (try 
    (let [status (.destroyStatus twitter (:id (@tweets statusnum)))]
      (str 
        "Success delete: @" 
        (.. status getUser getScreenName)
        " - "
        (.. status getText)))
    (catch Exception e "something has wrong.")))

; リプライ
(defn reply [statusnum & texts]
  "Reply to tweets"
  (let [reply (str \@ (:user (@tweets statusnum)) " " (apply str texts))]
    (do
      (println (str (apply str (take 137 (seq reply)))))
      (str "Success:" 
        (.getText
          (.updateStatus 
            twitter 
            (doto
              (StatusUpdate. 
                (if 
                  (> (count (seq reply)) 140)
                  (str (apply str (take 137 (seq reply))) "...")
                  (str \@ (:user (@tweets statusnum)) " " (apply str texts))))
              (.inReplyToStatusId (:id (@tweets statusnum))))))))))

; デバック用
(defn reload []
  (do
    (load-file "src/grimoire/commands.clj")
    (load-file "src/grimoire/response.clj")))
