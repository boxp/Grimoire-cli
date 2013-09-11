(ns grimoire.listener
  (:gen-class)
  (:import (twitter4j UserStreamListener))
  (:use [grimoire.oauth]
        [grimoire.datas]))

; Userstream status listener
(defn listener []
  (reify UserStreamListener

    (onStatus [this status]
      (let [newstatus (str (.. status getUser getScreenName)
                           (.. status getText)
                           (.. status getCreatedAt)
                           (.. status getSource)
                           (.. status getInReplyToStatusId)
                           (.. status getRetweetCount)
                           (.. status isFavorited)
                           (.. status getId)
                           (count tweets))]
        (do
          (if 
            (some #(= (.. status getId) %) @friends)
            (dosync
              (alter mentions conj newstatus)))
          (.add tweets newstatus)
          (print 
            (str
              (- (count @tweets) 1)
              " @"
              (.. status getUser getScreenName) 
              " - " 
              (.. status getText)
              "\n")))))

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
