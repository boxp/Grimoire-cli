(in-ns `grimoire.core)

(defn post [input]
  (try (str "Success:" (.getText (.updateStatus twitter input)))
           (catch Exception e (println "Something has wrong."))))

(defn showtl []
  (let [status (.getHomeTimeline twitter)]
  (do (println "Showing home timeline.")
      (apply println (.getUser status) ":" (.getText status)))))

(defn reload []
  (load-file "src/grimoire/core_commands.clj"))
