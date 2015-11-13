(ns ^:figwheel-always om-next-03.core
  (:import [goog.net XhrIo])
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require
    [cljs.pprint]
    [goog.dom :as gdom]
    [cognitect.transit :as tt]
    [om.next :as om :refer-macros [defui]]
    [om.dom :as dom]
    [clojure.test.check :as ck]
    [clojure.test.check.generators :as ckgs]
    [clojure.test.check.properties :as ckps]
    [cljs.core.async :refer [chan put!]]))

(enable-console-print!)

(println "Edits to this text should show up in your developer console.")

(defn remove-episode [episodes ep]
  (filter (fn [it] (not= it ep)) episodes))

(defn move-episode [episodes from to]
  (let [val (episodes from) parts (split-at to (remove-episode episodes val)) part1 (first parts) part2 (second parts)]
    (vec (concat part1 [val] part2))))

(defn reconciler-send [url]
  "result takes a callback to receive json response"
  (fn [m cb]
    (.send XhrIo url
           (fn [_]
             (this-as this
               (let [x (tt/read (tt/reader :json) (.getResponseText this))]
                 (cb x)))))))

(defui Episode
       static om/Ident
       (ident [this {:keys [episode]}]
              [:episode episode])
       static om/IQuery
       (query [this]
              [:episode :title :released :imdbRating :imdbID])
       Object
       (render [this]
               (let [{:keys [episode title imdbRating imdbID]} (om/props this)]
                 (dom/li nil
                         (dom/div #js {:className   "js-dragging"
                                       :draggable   true
                                       :onDragStart (fn [e] (let [datr (.-dataTransfer e) {:keys [drag-start index]} (om/get-computed this)]
                                                              (set! (.-effectAllowed datr) "move")
                                                              (.setData datr "text/html" "")
                                                              (drag-start e index)))
                                       :onDragEnd   (fn [e] (let [ident (om/get-ident this)]))
                                       :onDragOver  (fn [e] (let [ident (om/get-ident this)]
                                                              (.preventDefault e)
                                                              (-> e .-dataTransfer .-dropEffect (set! "move"))))
                                       :onDrop      (fn [e] (let [{:keys [drag-drop index]} (om/get-computed this)]
                                                              (drag-drop e index)))}
                                  (dom/h3 nil (str episode ". " title))
                                  (dom/a #js {:href (str "http://www.imdb.com/title/" imdbID)} "imdb")
                                  (dom/label nil "rating:") (dom/span nil imdbRating))))))

(def episode-ui (om/factory Episode {:keyfn :episode}))

(defui Episodes
       static om/IQuery
       (query [this]
              [{:episodes (om/get-query Episode)}])
       Object
       (drag-start [this e index]
                   (om/update-state! this assoc :dragged-index index))

       (drag-end [this e]
                 (om/update-state! this dissoc :dragged-index))

       (drag-over [this e]
                  (.preventDefault e))

       (drag-drop [this e index]
                  (let [from-index (:dragged-index (om/get-state this))
                        to-index index]
                    (.preventDefault e)
                    (om/transact! this `[(episodes/move {:move [~from-index ~to-index]}) :episodes]))) ;; `[(query) read)] mutate specification
       (render [this]
               (let [{:keys [episodes]} (om/props this)]
                 (dom/div nil
                          (dom/ol nil
                                  (map-indexed
                                    (fn [idx ep]
                                      (episode-ui (om/computed ep
                                                               {:index      idx ;; use the index to help with drag drop
                                                                :drag-start (fn [e k] (.drag-start this e k)) ;; could use idx param here
                                                                :drag-end   #(.drag-end this %)
                                                                :drag-over  #(.drag-over this %)
                                                                :drag-drop  (fn [e k] (.drag-drop this e k))})))
                                    episodes))))))

(defmulti reading om/dispatch)

(defmethod reading :episodes
  [{:keys [state ast]} key _]
  (let [st @state]
    (if (contains? st key)
      {:value (get st key)}                                 ;; loads data from app state
      {:remote-episodes true})))                            ;; loads data from external file /episodes.json

(defmethod reading :episode/dragged
  [{:keys [state]} key _]
  (let [st @state]
    {:value (get st key)}))

(defmulti mutating om/dispatch)

(defmethod mutating 'episodes/move
  [{:keys [state]} _ {:keys [move]}]
  {:value {:keys [:episodes]}
   :action
          (fn [] (swap! state update :episodes move-episode (first move) (second move)))})

(def reconciler
  (om/reconciler
    {:state     (atom {})
     :normalize false
     :remotes   [:remote-episodes]                          ;; vector of remotes
     :parser    (om/parser {:read reading :mutate mutating})
     :send      (reconciler-send "/episodes.json")}))

(om/add-root! reconciler Episodes (gdom/getElement "ui"))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
  )