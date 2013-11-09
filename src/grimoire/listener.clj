(ns grimoire.listener
  (:gen-class)
  (:import (twitter4j UserStreamListener)
           (javafx.scene.text Text Font)
           (javafx.scene.control Hyperlink)
           (javafx.scene.layout HBox VBox Priority)
           (javafx.scene Node)
           (javafx.scene.image Image ImageView)
           (javafx.application Platform)
           (javax.imageio ImageIO)
           (java.lang Runnable)
           (java.lang Boolean)
           (javafx.event EventHandler)
           (javafx.scene.paint Color))
  (:use [grimoire.oauth]
        [clojure.string :only [split]]
        [grimoire.data]
        [grimoire.commands]))

; Userstream status listener
(defn listener []
  (reify UserStreamListener
    (onStatus [this status]
      (future
        (do
          (dosync
            (alter tweets conj status))
          (if
            (= (.. twitter getScreenName) (.. status getInReplyToScreenName)) 
            (do
              (add-runlater
                (add-nodes! mention-nodes status))
              (add-runlater
                (add-nodes! nodes status)))
            (add-runlater
              (add-nodes! nodes status)))
          (try
            (doall
              (map #(.on-status % status) @plugins))
            (catch Exception e (print-node! (.getMessage e)))))))

    (onDeletionNotice [this statusDeletionNotice]
      (do
        (try
          (doall
            (map #(.on-del % statusDeletionNotice) @plugins))
          (catch Exception e (print-node! (.getMessage e))))
        (loop [coll nodes]
          (if (or (= (.. statusDeletionNotice getStatusId)
                   (.getId (@tweet-maps (first coll))))
                (nil? (first coll)))
            (add-runlater
              (.remove nodes (first coll)))
            (recur (rest coll))))))

    (onTrackLimitationNotice [this numberOfLimitedStatuses]
      (do
        (print-node! 
          "Got a track limitation notice:" 
          numberOfLimitedStatuses)))

    (onScrubGeo [this userId upToStatusId]
      (do
        (print-node! 
          "Got scrub_geo event userId:" 
          userId 
          "upToStatusId:" 
          upToStatusId)))

    (onStallWarning [this warning]
      (do
        (print-node! 
          "Got stall warning:" 
          warning)))

    (onFriendList [this friendIds]
      (do
        (dosync
          (alter friends conj friendIds))))

    (onFavorite [this source target favoritedStatus]
      (let [notice (gen-notice 
              "You Gotta Fav! source:@" 
              (.getScreenName source) 
              " target:@" 
              (.getScreenName target) 
              " @ " 
              (.. favoritedStatus getUser getScreenName) 
              " -" 
              (.getText favoritedStatus))]
        (do
          (try
            (doall
              (map #(.on-fav % source target favoritedStatus) @plugins))
            (catch Exception e (print-node! (.getMessage e))))
          (add-nodes! nodes notice)
          (add-nodes! mention-nodes notice))))

    (onUnfavorite [this source target unfavoritedStatus]
      (do
        (try
          (doall
            (map #(.on-unfav % source target unfavoritedStatus) @plugins))
          (catch Exception e (print-node! (.getMessage e))))
        (.add mention-nodes 0
          (print-node!
            "Catched unFav! source:@" 
            (.getScreenName source) 
            " target:@" 
            (.getScreenName target) 
            " @ " 
            (.. unfavoritedStatus getUser getScreenName) 
            " -" 
            (.getText unfavoritedStatus)))))

    (onFollow [this source followedUser]
      (do
        (try 
          (doall
            (map #(.on-follow % source followedUser) @plugins))
          (catch Exception e (print-node! (.getMessage e))))
        (print-node!
          "onFollow source:@" 
          (.getScreenName source) 
          " target:@" 
          (.getScreenName followedUser))
        (.add mention-nodes 0
          (gen-notice
            "onFollow source:@" 
            (.getScreenName source) 
            " target:@" 
            (.getScreenName followedUser)))))

    (onDirectMessage [this directMessage]
      (do
        (try
          (doall
            (map #(.on-follow % directMessage) @plugins))
          (catch Exception e (print-node! (.getMessage e))))
        (.add mention-nodes 0
          (print-node! 
            "onDirectMessage text:" 
            (.getText directMessage)))))

    (onUserListMemberAddition [this addedMember listOwner alist]
      (print-node! 
        (.getScreenName addedMember) 
        "listOwner:@" 
        (.getScreenName listOwner) 
        "list:" 
        (.getName alist)))
      
    (onUserListMemberDeletion [this deletedMember listOwner alist]
      (print-node! 
        (.getScreenName deletedMember) 
        "listOwner:@" 
        (.getScreenName listOwner) 
        "list:" 
        (.getName alist)))

    (onUserListSubscription [this subscriber listOwner alist]
      (print-node! 
        "onUserListSubscribed subscriber:@" 
        (.getScreenName subscriber) 
        " listOwner:@"
        (.getScreenName listOwner) 
        "list:" 
        (.getName alist)))

    (onUserListUnsubscription [this subscriber listOwner alist]
      (print-node! 
        "onUserListUnSubscribed subscriber:@" 
        (.getScreenName subscriber) 
        " listOwner:@"
        (.getScreenName listOwner) 
        "list:" 
        (.getName alist)))

    (onUserListCreation [this listOwner alist]
      (print-node!
        "onUserListCreated listOwner:@"
        (.getScreenName listOwner)
        " list:"
        (.getName alist)))

    (onUserListUpdate [this listOwner alist]
      (print-node!
        "onUserListUpdated listOwner:@"
        (.getScreenName listOwner)
        " list:"
        (.getName alist)))

    (onUserListDeletion [this listOwner alist]
      (print-node!
        "onUserListDestroyed listOwner:@"
        (.getScreenName listOwner)
        " list:"
        (.getName alist)))

    (onUserProfileUpdate [this updatedUser]
      (do
        (print-node! 
          "onUserProfileUpdated user:@"
          (.getScreenName updatedUser))))

    (onBlock [this source blockedUser]
      (do
        (.add mention-nodes 0
          (print-node!
            "onBlock user:@"
            (.getScreenName source)
            " target:@"
            (.getScreenName blockedUser)))))

    (onUnblock [this source unblockedUser]
      (do
        (.add mention-nodes 0
          (print-node! 
            "onUnBlock user:@"
            (.getScreenName source)
            " target:@"
            (.getScreenName unblockedUser)))))

    (onException [this ex]
      (do
        (.printStackTrace ex)
        (print 
          "onException:"
          (.getMessage ex))))))

; Userstream status listener (console)
(defn c-listener []
  (reify UserStreamListener
    (onStatus [this status]
      (do
        (dosync
          (alter tweets conj status))
        (if 
          (some #(= (.. twitter getScreenName) %) (map #(.getText %) (.. status getUserMentionEntities)))
          (print "->"))
        (print
          (.. status getUser getScreenName)
          ":"
          (.. status getText)
          (str (.. status getCreatedAt))
          (get-source (.. status getSource))
          (.indexOf @tweets status)
          "\n")

        (if 
          (some #(= (.. twitter getScreenName) %) 
            (map #(.getScreenName %)
              (.. status getUserMentionEntities)))
          (dosync
            (alter mentions conj status)))))

    (onDeletionNotice [this statusDeletionNotice]
      (do
        (print
          "Got a status deletion notice id:" 
          (.. statusDeletionNotice getStatusId)
          "\n")))

    (onTrackLimitationNotice [this numberOfLimitedStatuses]
      (do
        (print
          "Got a track limitation notice:" 
          numberOfLimitedStatuses
          "\n")))

    (onScrubGeo [this userId upToStatusId]
      (do
        (print
          "Got scrub_geo event userId:" 
          userId 
          "upToStatusId:" 
          upToStatusId
          "\n")))

    (onStallWarning [this warning]
      (do
        (print
          "Got stall warning:" 
          warning
          "\n")))

    (onFriendList [this friendIds]
      (do
        (dosync
          (alter friends conj friendIds))))

    (onFavorite [this source target favoritedStatus]
      (do
        (print
          "You Gotta Fav! source:@" 
          (.getScreenName source) 
          " target:@" 
          (.getScreenName target) 
          " @ " 
          (.. favoritedStatus getUser getScreenName) 
          " -" 
          (.getText favoritedStatus)
          "\n")))

    (onUnfavorite [this source target unfavoritedStatus]
      (do
        (print
          "Catched unFav! source:@" 
          (.getScreenName source) 
          " target:@" 
          (.getScreenName target) 
          " @ " 
          (.. unfavoritedStatus getUser getScreenName) 
          " -" 
          (.getText unfavoritedStatus)
          "\n")))

    (onFollow [this source followedUser]
      (do
        (print
          "onFollow source:@" 
          (.getScreenName source) 
          " target:@" 
          (.getScreenName followedUser)
          "\n")))

    (onDirectMessage [this directMessage]
      (print
        "onDirectMessage text:" 
        (.getText directMessage)
        "\n"))

    (onUserListMemberAddition [this addedMember listOwner alist]
      (print
        (.getScreenName addedMember) 
        "listOwner:@" 
        (.getScreenName listOwner) 
        "list:" 
        (.getName alist)
        "\n"))
      
    (onUserListMemberDeletion [this deletedMember listOwner alist]
      (print
        (.getScreenName deletedMember) 
        "listOwner:@" 
        (.getScreenName listOwner) 
        "list:" 
        (.getName alist)
        "\n"))

    (onUserListSubscription [this subscriber listOwner alist]
      (print
        "onUserListSubscribed subscriber:@" 
        (.getScreenName subscriber) 
        " listOwner:@"
        (.getScreenName listOwner) 
        "list:" 
        (.getName alist)
        "\n"))

    (onUserListUnsubscription [this subscriber listOwner alist]
      (print
        "onUserListUnSubscribed subscriber:@" 
        (.getScreenName subscriber) 
        " listOwner:@"
        (.getScreenName listOwner) 
        "list:" 
        (.getName alist)
        "\n"))

    (onUserListCreation [this listOwner alist]
      (print
        "onUserListCreated listOwner:@"
        (.getScreenName listOwner)
        " list:"
        (.getName alist)
        "\n"))

    (onUserListUpdate [this listOwner alist]
      (print
        "onUserListUpdated listOwner:@"
        (.getScreenName listOwner)
        " list:"
        (.getName alist)
        "\n"))

    (onUserListDeletion [this listOwner alist]
      (print
        "onUserListDestroyed listOwner:@"
        (.getScreenName listOwner)
        " list:"
        (.getName alist)
        "\n"))

    (onUserProfileUpdate [this updatedUser]
      (do
        (print
          "onUserProfileUpdated user:@"
          (.getScreenName updatedUser)
          "\n")))

    (onBlock [this source blockedUser]
      (do
        (print
          "onBlock user:@"
          (.getScreenName source)
          " target:@"
          (.getScreenName blockedUser)
          "\n")))

    (onUnblock [this source unblockedUser]
      (do
        (print
          "onUnBlock user:@"
          (.getScreenName source)
          " target:@"
          (.getScreenName unblockedUser)
          "\n")))

    (onException [this ex]
      (do
        (.printStackTrace ex)
        (print 
          "onException:"
          (.getMessage ex)
          "\n")))))

