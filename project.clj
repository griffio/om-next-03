(defproject om-tutorial "0.1.0-SNAPSHOT"
  :description "A Tutorial for Om 1.0.0 (next)"
  :dependencies [[org.clojure/clojure "1.8.0" :scope "provided"]
                 [org.clojure/clojurescript "1.8.51" :scope "provided"]
                 [devcards "0.2.1-6" :exclusions [org.omcljs/om]]
                 [datascript "0.13.3"]
                 [com.cognitect/transit-cljs "0.8.225"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [org.omcljs/om "1.0.0-alpha38"]
                 [figwheel-sidecar "0.5.4-3" :scope "test"]]

  :source-paths ["src/main/cljs" "src/cards/cljs"]

  :clean-targets ^{:protect false} ["resources/public/js" "resources/public/cards" "target"]

  :figwheel {:build-ids   ["dev" "cards"]
             :server-port 3449}

  :cljsbuild {
              :builds
              [
               {:id           "dev"
                :figwheel     true
                :source-paths ["src/main/cljs"]
                :compiler     {:main                 om-next-03.core
                               :asset-path           "js"
                               :output-to            "resources/public/js/main.js"
                               :output-dir           "resources/public/js"
                               :recompile-dependents true
                               :parallel-build       true
                               :verbose              false}}

               {:id           "cards"
                :figwheel     {:devcards true}
                :source-paths ["src/main/cljs" "src/cards/cljs"]
                :compiler     {
                               :main                 om-next-03.cards
                               :source-map-timestamp true
                               :asset-path           "cards"
                               :output-to            "resources/public/cards/cards.js"
                               :output-dir           "resources/public/cards"
                               :recompile-dependents true
                               :parallel-build       true
                               :verbose              false}}]}


  :profiles {
             :dev {:source-paths ["src/dev"]
                   :repl-options {:init-ns user
                                  :port    7001}}})



