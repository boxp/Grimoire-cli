(defproject grimoire "0.0.5-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                  [org.clojure/core.async "0.1.242.0-44b1e3-alpha"]
                 [org.twitter4j/twitter4j-core"[3.0,)"]
                 [org.twitter4j/twitter4j-stream"[3.0,)"]
                 [org.twitter4j/twitter4j-async"[3.0,)"]
                 [enlive "1.1.4"]
                 [org.clojure/tools.nrepl "0.2.3"]
                 [clojure-jsr223/clojure-jsr223 "1.0"]]
  :main grimoire.core
  :aot [grimoire.gui]
  :resource-paths ["resources" "jfxrt.jar"]
  :java-source-paths ["src/java"])
