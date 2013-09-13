(ns grimoire.datas
  (:import (javafx.collections FXCollections ObservableList)))

(def nodes #^javafx.collections.ObservableList (FXCollections/observableArrayList (java.util.ArrayList. [])))
(def tweets (ref []))
(def mentions (ref []))
(def friends (ref #{}))
