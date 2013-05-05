(ns grimoire.listener
  (:import (twitter4j UserStreamListener)))

(def tweets #{})
(def friends #{})

(def listener 
  (proxy [UserStreamListener] []
    (onStatus [status]
      (do
        (def tweets 
          (conj tweets 
            (.getId status) 
            {:user (.. status getUser getScreenName)
             :text (.. status getText)
             :time (.. status getCreatedAt)
             :source (.. status getSource)
             :inreply (.. status getInReplyToStatusId)
             :retweeted (.. status getRetweetCount)
             :favorited? (.. status isFavorited)}))
        (println 
          "onStatus @" 
          (.. status getUser getScreenName) 
          "-" 
          (.. status getText))))

    (onDeletionNotice [statusDeletionNotice]
      (do
        (println 
          "Got a status deletion notice id:" 
          (.. statusDeletionNotice getStatusId))))

    (onTrackLimitationNotice [numberOfLimitedStatuses]
      (do
        (println 
          "Got a track limitation notice:" 
          numberOfLimitedStatuses)))

    (onScrubGeo [userId upToStatusId]
      (do
        (println 
          "Got scrub_geo event userId:" 
          userId 
          "upToStatusId:" 
          upToStatusId)))

    (onStallWarning [warning]
      (do
        (println 
          "Got stall warning:" 
          warning)))

    (onFriendList [friendIds]
      (do
        (def friends 
          (conj friends friendIds))))

    (onFavorite [source target favoritedStatus]
      (do
        (println 
          "You Gotta Fav! source:@" 
          (.getScreenName source) 
          " target:@" 
          (.getScreenName target) 
          " @ " 
          (.. favoritedStatus getUser getScreenName) 
          " -" 
          (.getText favoritedStatus))))

    (onUnfavorite [source target unfavoritedStatus]
      (do
        (println 
          "Catched unFav! source:@" 
          (.getScreenName source) 
          " target:@" 
          (.getScreenName target) 
          " @ " 
          (.. unfavoritedStatus getUser getScreenName) 
          " -" 
          (.getText unfavoritedStatus))))

    (onFollow [source followedUser]
      (do
        (println 
          "onFollow source:@" 
          (.getScreenName source) 
          " target:@" 
          (.getScreenName followedUser))))

    (onDirectMessage [directMessage]
      (println 
        "onDirectMessage text:" 
        (.getText directMessage)))

    (onUserListMemberAddition [addedMember listOwner alist]
      (println 
        (.getScreenName addedMember) 
        "listOwner:@" 
        (.getScreenName listOwner) 
        "list:" 
        (.getName alist)))
      
    (onUserListMemberDeletion [deletedMember listOwner alist]
      (println 
        (.getScreenName deletedMember) 
        "listOwner:@" 
        (.getScreenName listOwner) 
        "list:" 
        (.getName alist)))

    (onUserListSubscription [subscriber listOwner alist]
      (println 
        "onUserListSubscribed subscriber:@" 
        (.getScreenName subscriber) 
        " listOwner:@"
        (.getScreenName listOwner) 
        "list:" 
        (.getName alist)))

    (onUserListUnsubscription [subscriber listOwner alist]
      (println 
        "onUserListUnSubscribed subscriber:@" 
        (.getScreenName subscriber) 
        " listOwner:@"
        (.getScreenName listOwner) 
        "list:" 
        (.getName alist)))

    (onUserListCreation [listOwner alist]
      (println
        "onUserListCreated listOwner:@"
        (.getScreenName listOwner)
        " list:"
        (.getName alist)))

    (onUserListUpdate [listOwner alist]
      (println
        "onUserListUpdated listOwner:@"
        (.getScreenName listOwner)
        " list:"
        (.getName alist)))

    (onUserListDeletion [listOwner alist]
      (println
        "onUserListDestroyed listOwner:@"
        (.getScreenName listOwner)
        " list:"
        (.getName alist)))

    (onUserProfileUpdate [updatedUser]
      (do
        (println 
          "onUserProfileUpdated user:@"
          (.getScreenName updatedUser))))

    (onBlock [source blockedUser]
      (do
        (println 
          "onBlock user:@"
          (.getScreenName source)
          " target:@"
          (.getScreenName blockedUser))))

    (onUnBlock [source unblockedUser]
      (do
        (println 
          "onBlock user:@"
          (.getScreenName source)
          " target:@"
          (.getScreenName unblockedUser))))

    (onException [ex]
      (do
        (.printStackTrace ex)
        (println 
          "onException:"
          (.getMessage ex))))))
