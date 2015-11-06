(use 'figwheel-sidecar.repl-api)
(start-figwheel! {:all-builds (figwheel-sidecar.config/get-project-builds)})
(cljs-repl)