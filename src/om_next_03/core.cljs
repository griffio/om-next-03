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

(defn drag-episode [episodes from to]
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
               (let [{:keys [episode title imdbRating imdbID]} (om/props this)
                     {:keys [drag-drop drag-end drag-over drag-start]} (om/get-computed this)] ;; computed delegate callbacks
                 (dom/li nil
                         (dom/div #js {:className   "js-dragging"
                                       :draggable   true
                                       :onDragStart (fn [e] (let [datr (.-dataTransfer e)]
                                                              (set! (.-effectAllowed datr) "move")
                                                              (.setData datr "text" title);; set anything, we are not using the data
                                                              (drag-start e)))
                                       :onDragEnd   (fn [e] (let []
                                                              (drag-end e)))
                                       :onDragOver  (fn [e] (let []
                                                              (.preventDefault e)
                                                              (-> e .-dataTransfer .-dropEffect (set! "move"))
                                                              (drag-over e)))
                                       :onDrop      (fn [e] (let []
                                                              (.preventDefault e)
                                                              (.stopPropagation e)
                                                              (drag-drop e)))}
                                  (dom/h3 nil (str episode ". " title))
                                  (dom/a #js {:href (str "http://www.imdb.com/title/" imdbID)} "imdb rating:")
                                  (dom/label nil imdbRating))))))

(def episode-ui (om/factory Episode {:keyfn :episode}))

(defui Episodes
       static om/IQuery
       (query [this]
              [{:episodes (om/get-query Episode)}])
       Object
       ;; drag event ordering: start, over, drop, end
       (drag-start [this e index]
                   (om/update-state! this assoc :dragged-index index)) ;; track the dragged index in the parent state
       (drag-over [this e index])

       (drag-drop [this e index]
                  (let [from-idx (:dragged-index (om/get-state this)) ;; get the dragged episode index for drop event
                        to-idx index]
                    (om/transact! this `[(episodes/drag {:from ~from-idx :to ~to-idx})]))) ;; `[(query) read)] - https://github.com/omcljs/om/wiki/Om-Next-FAQ
       (drag-end [this e index]
                 (om/update-state! this dissoc :dragged-index)) ;; dragging has completed

       (render [this]
               (let [{:keys [episodes]} (om/props this)]
                 (dom/div nil
                          (dom/ol nil
                                  (->> episodes
                                       (map-indexed
                                         (fn [idx ep]
                                           (episode-ui
                                             (om/computed ep
                                                          {:drag-start (fn [e] (.drag-start this e idx)) ;; associate the episode ordinal index with each callback
                                                           :drag-end   (fn [e] (.drag-end this e idx))
                                                           :drag-over  (fn [e] (.drag-over this e idx))
                                                           :drag-drop  (fn [e] (.drag-drop this e idx))}))))))))))

(defmulti reading om/dispatch)

(defmethod reading :episodes
  [{:keys [state ast]} key _]
  (let [st @state]
    (if (contains? st key)
      {:value (get st key)}                                 ;; loads data from app state
      {:remote-episodes true})))                            ;; loads data from external file /episodes.json

(defmulti mutating om/dispatch)

(defmethod mutating 'episodes/drag
  [{:keys [state]} _ {:keys [from to]}]
  {:value {:keys [:episodes]}
   :action
          (fn [] (swap! state update :episodes drag-episode from to))})

(def reconciler
  (om/reconciler
    {:state     (atom {})
     :normalize false
     :remotes   [:remote-episodes]                          ;; vector of remotes
     :parser    (om/parser {:read reading :mutate mutating})
     :send      (reconciler-send "/episodes2.json")}))

(om/add-root! reconciler Episodes (gdom/getElement "ui"))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
  )