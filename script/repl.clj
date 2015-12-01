(ns user
  (:require
    [figwheel-sidecar.repl-api :as ra]))

(def figwheel-config
  {:figwheel-options {:server-port 3449}
   :build-ids        ["cards" "dev"]
   :all-builds       (figwheel-sidecar.config/get-project-builds)})


(defn start-dev
  ""
  []
  (ra/start-figwheel! figwheel-config)
  (ra/cljs-repl))

(start-dev)

