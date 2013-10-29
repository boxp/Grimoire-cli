(ns grimoire.plugin)

(defprotocol Plugin
  "Grimoire plugin protocol, but you don't have to override all methods." 
  (on-status [this status])
  (on-rt [this status])
  (on-unrt [this status])
  (on-fav [this source target status])
  (on-unfav [this source target status])
  (on-del [this status])
  (on-follow [this source user])
  (on-dm [this dm])
  (on-start [this])
  (on-click [this]))
