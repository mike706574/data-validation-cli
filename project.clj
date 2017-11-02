(defproject fun.mike/data-validation-cli "0.1.0-SNAPSHOT"
  :description "A CLI interface for validating some kind of data."
  :url "https://github.com/mike706574/data-validation-cli"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.9.0-beta3"]
                 [org.clojure/spec.alpha "0.1.134"]
                 [org.clojure/tools.cli "0.3.5"]
                 [org.clojure/core.match "0.3.0-alpha5"]
                 [com.taoensso/timbre "4.10.0"]
                 [fun.mike/syrup-alpha "0.0.13"]
                 [fun.mike/pancake "0.0.20"]
                 [org.clojure/test.check "0.9.0"]
                 [com.gfredericks/test.chuck "0.2.8"]]
  :profiles {:uberjar {:aot :all
                       :main example.main
                       :uberjar-name "data-validation.jar"}
             :dev {:source-paths ["dev"]
                   :target-path "target/dev"
                   :dependencies [[org.clojure/tools.namespace "0.2.11"]]}}
  :repl-options {:init-ns user})
