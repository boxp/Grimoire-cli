(defproject grimoire "0.0.5-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/core.async "0.1.242.0-44b1e3-alpha"]
                 [org.twitter4j/twitter4j-core"4.0.2"]
                 [org.twitter4j/twitter4j-stream"4.0.2"]
                 [org.twitter4j/twitter4j-async"4.0.2"]
                 [com.carrotsearch/java-sizeof "0.0.4"]
                 [enlive "1.1.4"]
                 [org.clojure/tools.nrepl "0.2.3"]
                 [gntp "0.6.0"]
                 [clojure-jsr223/clojure-jsr223 "1.0"]]
  :resource-paths ["resources"]
  :aot [grimoire.core]
  :main grimoire.core.App
  :uberjar {:aot :all
            :auto-clean false}
  :java-source-paths ["src/java"])
