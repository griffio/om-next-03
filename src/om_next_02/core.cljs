(ns ^:figwheel-always om-next-02.core
  (:require
    [cljs.pprint]
    [goog.dom :as gdom]
    [cognitect.transit :as tt]
    [om.next :as om :refer-macros [defui]]
    [om.dom :as dom]
    [om-next-02.utils :as util]
    [clojure.test.check :as ck]
    [clojure.test.check.generators :as ckgs]
    [clojure.test.check.properties :as ckps]))

(enable-console-print!)

(println "Edits to this text should show up in your developer console.")

(defui TelevisionShow
       static om/IQuery
       (query [this]
              [:tvshow/title :tvshow/year :tvshow/rated :tvshow/released :tvshow/runtime :tvshow/type])
       Object
       (render [this]
               (println (om/props this))
               (let [{:keys [tvshow/title tvshow/year tvshow/rated tvshow/released tvshow/runtime tvshow/type]} (om/props this)]
                 (dom/li nil
                         (dom/h3 nil title)
                         (dom/h4 nil (str type " " year))
                         (dom/label nil "Rated:") (dom/span nil rated)
                         (dom/label nil "Released:") (dom/span nil released)
                         (dom/label nil "Running Time:") (dom/span nil runtime)))))

(def television-show (om/factory TelevisionShow))

(defui TelevisionShows
       static om/IQuery
       (query [this]
              [:tvshows/title :tvshows/list])
       Object
       (render [this]
               (let [{:keys [tvshows/title tvshows/list]} (om/props this)]
                 (dom/div nil
                          (dom/h2 nil title)
                          (apply dom/ul nil
                                 (map television-show list))))))

(defmulti reading om/dispatch)

(defmethod reading :default
  [{:keys [state]} k _]
  (let [st @state]
    (if (contains? st k)
      {:value (get st k)}
      {:remote true})))

(defmulti mutating om/dispatch)

(defmethod mutating :default
  [_ _ _] {:remote true})

(def reconciler
  (om/reconciler
    {:state      (atom {})
     :normalize  true
     :merge-tree (fn [a b] (merge a b))
     :parser     (om/parser {:read reading :mutate mutating})
     :send       (util/reconciler-send "/tv-shows.json")}))

(om/add-root! reconciler TelevisionShows (gdom/getElement "ui"))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
  )