(ns grimoire.listener
  (:import (twitter4j UserStreamListener)))

(def tweets [])
(def friends #{})
(def mentions [])

(def listener 
  (proxy [UserStreamListener] []
    (onStatus [status]
      (let [newstatus {:user (.. status getUser getScreenName)
                       :text (.. status getText)
                       :time (.. status getCreatedAt)
                       :source (.. status getSource)
                       :inreply (.. status getInReplyToStatusId)
                       :retweeted (.. status getRetweetCount)
                       :favorited? (.. status isFavorited)
                       :id (.. status getId)
                       :count (count tweets)}]
        (do
          (if 
            (some #(= (.. status getId) %) friends)
            (def mentions
              (conj 
                mentions
                newstatus)))
          (def tweets 
            (conj 
              tweets 
              newstatus))
          (print 
            (str
              (- (count tweets) 1)
              " @"
              (.. status getUser getScreenName) 
              " - " 
              (.. status getText)
              "\n")))))

    (onDeletionNotice [statusDeletionNotice]
      (do
        (print
          "Got a status deletion notice id:" 
          (.. statusDeletionNotice getStatusId)
          "\n")))

    (onTrackLimitationNotice [numberOfLimitedStatuses]
      (do
        (print
          "Got a track limitation notice:" 
          numberOfLimitedStatuses
          "\n")))

    (onScrubGeo [userId upToStatusId]
      (do
        (print
          "Got scrub_geo event userId:" 
          userId 
          "upToStatusId:" 
          upToStatusId
          "\n")))

    (onStallWarning [warning]
      (do
        (print
          "Got stall warning:" 
          warning
          "\n")))

    (onFriendList [friendIds]
      (do
        (def friends 
          (conj friends friendIds))))

    (onFavorite [source target favoritedStatus]
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

    (onUnfavorite [source target unfavoritedStatus]
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

    (onFollow [source followedUser]
      (do
        (print
          "onFollow source:@" 
          (.getScreenName source) 
          " target:@" 
          (.getScreenName followedUser)
          "\n")))

    (onDirectMessage [directMessage]
      (print
        "onDirectMessage text:" 
        (.getText directMessage)
        "\n"))

    (onUserListMemberAddition [addedMember listOwner alist]
      (print
        (.getScreenName addedMember) 
        "listOwner:@" 
        (.getScreenName listOwner) 
        "list:" 
        (.getName alist)
        "\n"))
      
    (onUserListMemberDeletion [deletedMember listOwner alist]
      (print
        (.getScreenName deletedMember) 
        "listOwner:@" 
        (.getScreenName listOwner) 
        "list:" 
        (.getName alist)
        "\n"))

    (onUserListSubscription [subscriber listOwner alist]
      (print
        "onUserListSubscribed subscriber:@" 
        (.getScreenName subscriber) 
        " listOwner:@"
        (.getScreenName listOwner) 
        "list:" 
        (.getName alist)
        "\n"))

    (onUserListUnsubscription [subscriber listOwner alist]
      (print
        "onUserListUnSubscribed subscriber:@" 
        (.getScreenName subscriber) 
        " listOwner:@"
        (.getScreenName listOwner) 
        "list:" 
        (.getName alist)
        "\n"))

    (onUserListCreation [listOwner alist]
      (print
        "onUserListCreated listOwner:@"
        (.getScreenName listOwner)
        " list:"
        (.getName alist)
        "\n"))

    (onUserListUpdate [listOwner alist]
      (print
        "onUserListUpdated listOwner:@"
        (.getScreenName listOwner)
        " list:"
        (.getName alist)
        "\n"))

    (onUserListDeletion [listOwner alist]
      (print
        "onUserListDestroyed listOwner:@"
        (.getScreenName listOwner)
        " list:"
        (.getName alist)
        "\n"))

    (onUserProfileUpdate [updatedUser]
      (do
        (print
          "onUserProfileUpdated user:@"
          (.getScreenName updatedUser)
          "\n")))

    (onBlock [source blockedUser]
      (do
        (print
          "onBlock user:@"
          (.getScreenName source)
          " target:@"
          (.getScreenName blockedUser)
          "\n")))

    (onUnBlock [source unblockedUser]
      (do
        (print
          "onUnBlock user:@"
          (.getScreenName source)
          " target:@"
          (.getScreenName unblockedUser)
          "\n")))

    (onException [ex]
      (do
        (.printStackTrace ex)
        (print
          "onException:"
          (.getMessage ex)
          "\n")))))
