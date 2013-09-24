(ns grimoire.datas
  (:import (javafx.collections FXCollections ObservableList)))

; datas
(def tweets (ref [])) 
(def nodes (atom #^javafx.collections.ObservableList (FXCollections/observableArrayList (java.util.ArrayList. []))))
(def mentions (ref []))
(def friends (ref #{}))

; parameters
(def max-nodes (atom 100))
(def tweets-size (atom 13))
