(ns grimoire.datas
  (:import (javafx.collections FXCollections ObservableList)
           (javafx.scene Node)))

; twitter
(def tweets (ref [])) 
(def nodes (atom #^javafx.collections.ObservableList (FXCollections/observableArrayList (java.util.ArrayList. []))))
(def mentions (ref []))
(def friends (ref #{}))

; twitter userstream
(def on-status (ref (fn [status] nil)))
(def on-mention (ref (fn [status] nil)))
(def on-fav (ref (fn [status] nil)))
(def on-ret (ref (fn [status] nil)))

; system
(def max-nodes (atom 100))
(def tweets-size (atom 13))
(def nrepl-server (atom nil))

; javafx
(def main-stage (atom nil))
(def listv (atom nil))
(def mainscene (atom nil))
(def theme (atom nil))
(def pns (atom nil))
