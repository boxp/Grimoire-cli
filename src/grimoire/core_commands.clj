(in-ns `grimoire.core)

(defn post [input]
  (try (str "Success:" (.getText (.updateStatus twitter input)))
           (catch Exception e (println "Something has wrong."))))

(defn showtl []
  (let [statusAll (reverse (.getHomeTimeline twitter))]
    (loop [status statusAll i 1]
      (if (= i 20)
        nil
        (do
          (println (.getScreenName (.getUser (first status))) ":" (.getText (first status)))
          (recur (rest status) (+ i 1)))))))

(defn reload []
  (load-file "src/grimoire/core_commands.clj"))
