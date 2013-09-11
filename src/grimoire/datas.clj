(ns grimoire.datas
  (:import (javafx.collections FXCollections ObservableList)))

(def tweets #^javafx.collections.ObservableList (FXCollections/observableArrayList (java.util.ArrayList. [])))
(def mentions (ref []))
(def friends (ref #{}))
