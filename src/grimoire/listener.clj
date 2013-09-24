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
  (:require [net.cgrand.enlive-html :as en])
  (:use [grimoire.oauth]
        [grimoire.datas]
        [grimoire.commands]))

(defn get-source
  [source]
  (first 
    (:content 
      (first 
        (:content 
          (first 
            (:content
              (first
                (en/html-resource 
                    (java.io.StringReader.  source))))))))))

; Userstream status listener
(defn listener []
  (reify UserStreamListener

    (onStatus [this status]
      (let [
            node (doto
                   (HBox.)
                   (.setSpacing 10)
                   (.setPrefWidth 500))
            favb (doto (Hyperlink.)
                   (.setText "★")
                   (.setFont (Font. 10))
                   (.setTextFill (Color/BLACK)))
            retb (doto (Hyperlink.)
                   (.setText "∞")
                   (.setFont (Font. 10))
                   (.setTextFill (Color/BLACK)))
            favretb (doto (Hyperlink.)
                      (.setText "★∞")
                      (.setFont (Font. 10))
                      (.setTextFill (Color/BLACK)))
            delb (doto (Hyperlink.)
                   (.setFont (Font. 10))
                   (.setText "×"))
            newstatus {:user (.. status getUser getScreenName)
                       :text (.. status getText)
                       :time (.. status getCreatedAt)
                       :source (.. status getSource)
                       :inreply (.. status getInReplyToStatusId)
                       :isretweet? (.. status isRetweet)
                       :retweeted (.. status getRetweetCount)
                       :favorited? (.. status isFavorited)
                       :id (.. status getId)
                       :count (count @tweets)
                       :image (.. status getUser getBiggerProfileImageURL)}
            source (get-source (.. status getSource))
            image (Image. (^String .. status getUser getBiggerProfileImageURL) (double 48) (double 48) true true)
            imageview (ImageView. image)
            uname (doto (Text. (.. status getUser getScreenName))
                    (.setWrappingWidth 440))
            info (doto 
                   (Text. 
                     (str (.. status getCreatedAt) " " source " " (count @tweets)))
                   (.setFont (Font. 10)))
            vbox (VBox.)
            hbox (doto (HBox.)
                   (.setSpacing 5))
            text (doto (Text. (.. status getText))
                   (.setFont (Font. @tweets-size))
                   (.. wrappingWidthProperty (bind (.. node widthProperty (add -150)))))]
        (do
          (doto (. hbox getChildren)
            (.add info)
            (.add favb)
            (.add retb)
            (.add favretb))
          (if (= (newstatus :user) (.getScreenName twitter))
            (.. hbox getChildren (add delb)))
          (doto (. vbox getChildren) 
            (.add uname)
            (.add text)
            (.add hbox))
          (doto (. node getChildren) 
            (.add imageview)
            (.add vbox))
          (doto favb
            (.setOnMouseClicked (reify EventHandler
                                  (handle [this me]
                                    (let [favb (.getSource me)]
                                      (if (= (.getTextFill favb) (Color/BLACK))
                                        (do
                                          (.setTextFill favb (Color/ORANGE))
                                          (fav (newstatus :count)))
                                        (do
                                          (.setTextFill favb (Color/BLACK))
                                          (unfav (newstatus :count)))))))))
          (doto retb
            (.setOnMouseClicked (reify EventHandler
                                  (handle [this me]
                                    (let [retb (.getSource me)]
                                      (if (= (.getTextFill retb) (Color/BLACK))
                                        (do
                                          (.setTextFill retb (Color/CYAN))
                                          (ret (newstatus :count)))
                                        (do
                                          (.setTextFill retb (Color/BLACK))
                                          (unret (newstatus :count)))))))))
          (doto favretb 
            (.setOnMouseClicked (reify EventHandler
                                  (handle [this me]
                                    (let [favretb (.getSource me)]
                                      (if (= (.getTextFill favretb) (Color/BLACK))
                                        (do
                                          (.setTextFill favretb (Color/ORANGE))
                                          (favret (newstatus :count)))
                                        (do
                                          (.setTextFill favretb (Color/BLACK))
                                          (unfavret (newstatus :count)))))))))
          (doto delb 
            (.setOnMouseClicked (reify EventHandler
                                  (handle [this me]
                                    (let [delb (.getSource me)]
                                      (if (= (.getTextFill delb) (Color/BLACK))
                                        (do
                                          (.setTextFill delb (Color/RED))
                                          (favret (newstatus :count)))
                                        (do
                                          (.setTextFill delb (Color/BLACK))
                                          (unfavret (newstatus :count)))))))))
          (HBox/setHgrow text Priority/SOMETIMES)
          (HBox/setHgrow vbox Priority/SOMETIMES)
          (HBox/setHgrow node Priority/SOMETIMES)
          (if 
            (some #(= (.. status getId) %) @friends)
            (dosync
              (alter mentions conj newstatus)))
          (dosync
            (alter tweets conj newstatus))
          (Platform/runLater
            (reify Runnable
              (run [this]
                (do
                  (.add @nodes 0 node)
                  (if (> (.size @nodes) @max-nodes)
                    (.remove @nodes @max-nodes (.size @nodes))))))))))

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
