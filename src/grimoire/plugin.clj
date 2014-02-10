(ns grimoire.plugin
  (:import (java.io File))
  (:use [grimoire.commands]
        [grimoire.data]))

(defn add-plugin!
  "Plugin�v���g�R�����p�������I�u�W�F�N�g���v���O�C���ɓo�^���܂�"
  [item]
  (dosync
    (alter plugins conj item)))

(defn load-plugin
  "plugin�����[�h���܂�"
  []
  (let [loaded  (filter #(= (seq ".clj") (seq (take-last 4 %)))
                  (.list
                    (File. 
                      (str (get-home) "/.grimoire/plugin"))))]
    (do
      (dosync
        (alter plugins empty))
      (binding [*ns* (find-ns 'grimoire.plugin)]
        (doall
          (map #(add-plugin! 
                 (load-file 
                   (str (get-home) "/.grimoire/plugin/" %)))
            loaded))))))

(load-plugin)
