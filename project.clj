(defproject grimoire "0.0.5-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [dollswar "0.1.0-SNAPSHOT"]
                 [org.twitter4j/twitter4j-core"[3.0,)"]
                 [org.twitter4j/twitter4j-stream"[3.0,)"]
                 [local.oracle/javafxrt "2.2.21"]]
  :main grimoire.core
  :aot [grimoire.core grimoire.listener grimoire.login-form grimoire.fxsession]
  :java-source-paths ["src/java"])
