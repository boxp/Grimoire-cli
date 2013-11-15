(ns grimoire.data
  (:import (javafx.collections FXCollections ObservableList)
           (javafx.scene Node)))

; twitter
(def tokens 
  (atom nil))
(def subtokens
  (ref {}))
(def oauthtoken
  (atom nil))
(def tweets 
  "Received status vector"
  (ref [])) 
(def tweet-maps 
  "Node -> status map"
  (ref {}))
(def mentions
  "Received mentions (deplicated)"
  (ref []))
(def friends 
  "Received friends list (deplicated)"
  (ref #{}))
(def myname
  "My twitter screen name"
  (atom nil))
(def imagemap
  "Profile images map"
  (ref {}))
(def twitter
  "Main twitter instance"
  (atom nil))
(def twitters
  "Sub twitter instances"
  (ref {}))

; plugin
(def plugins 
  "プラグインが収納される集合"
  (ref #{}))

(defprotocol Plugin
  "Grimoireのプラグインを示すプロトコル，プラグインを作るにはreify,proxyを用いて継承し，pluginsに追加して下さい．" 
  (get-name [this])
  (on-status [this status])
  (on-rt [this status])
  (on-unrt [this status])
  (on-fav [this source target status])
  (on-unfav [this source target status])
  (on-del [this status])
  (on-follow [this source user])
  (on-dm [this dm])
  (on-start [this])
  (on-click [this e]))

; system
(def max-nodes 
  "Var max node (deplicated)"
  (atom 100))
(def tweets-size 
  "Var tweets text size"
  (atom 13))
(def nrepl-server 
  "Var nrepl-server"
  (atom nil))
(def browser 
  "Var to use browser"
  (atom "chromium"))
(def key-maps 
  "Key Shortcuts hash-map, key: [key on-ctrl? on-shift?] value: function"
  (ref {}))
(def nervous
  "Use dialog warning"
  (atom false))

; javafx
(def main-stage (atom nil))
(def mainscene (atom nil))
(def theme 
  "Var grimoire's theme"
  (atom ""))
(def nodes 
  "Listview's Item array"
  (FXCollections/observableArrayList))
(def mention-nodes 
  "Listview's Item array"
  (FXCollections/observableArrayList))
