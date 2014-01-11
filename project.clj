(defproject dojo "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :main dojo.core
  
  :dependencies [[org.clojure/clojure "1.5.1"]

                 [compojure "1.1.6"]
                 [jarohen/chord "0.2.2"]
                 [ring/ring-core "1.2.0"]
                 [hiccup "1.0.4"]

                 [org.clojure/clojurescript "0.0-2138"]
                 [org.clojure/tools.reader "0.8.3"]
                 [prismatic/dommy "0.1.2"]
                 [om "0.1.5"]]

  :plugins [[lein-pdo "0.1.1"]
            [lein-cljsbuild "1.0.1"]
            [jarohen/lein-frodo "0.2.9"]]

  :frodo/config-resource "config/dojo.edn"

  :source-paths ["src/clj"]

  :aliases {"dev" ["pdo" "cljsbuild" "auto" "dev," "frodo"]
            "prod" ["pdo" "cljsbuild" "once" "release," "frodo"]}

  :resource-paths ["resources" "target/resources"]

  :cljsbuild { 
    :builds [{:id "dev"
              :source-paths ["src/cljs"]
              :compiler {
                :output-to "target/resources/js/dojo.js"
                :output-dir "target/resources/out"
                :optimizations :none
                :source-map true}}

              {:id "release"
              :source-paths ["src/cljs"]
              :compiler {
                :output-to "target/resources/js/dojo.js"
                :optimizations :advanced
                :pretty-print false
                :preamble ["react/react.min.js"]
                :externs ["react/externs/react.js"]}}
                ]}

)
