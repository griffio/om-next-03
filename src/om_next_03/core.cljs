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

(defn remove-it [vec val]
  (filter (fn [n] (not= n val)) vec))

(defn move-it [vec from to]
  (let [val (vec from) parts (split-at to (remove-it vec val)) part1 (first parts) part2 (second parts)]
    (concat part1 [val] part2)))

(defn move-episode [state from to]
  (println "from " from " to" to)
  state)

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
       (ident [this props]
              [:episode/by-id (:episode props)])
       static om/IQuery
       (query [this]
              [:episode :title :released :imdbRating :imdbID])
       Object
       (render [this]
               (let [{:keys [episode title imdbRating imdbID]} (om/props this) {:keys [drag-start drag-drop]} (om/get-computed this)]
                 (dom/li nil
                         (dom/div #js {:className   "js-dragging"
                                       :draggable   true
                                       :onDragStart (fn [e] (let [datr (.-dataTransfer e) ident (om/get-ident this)]
                                                              (set! (.-effectAllowed datr) "move")
                                                              (.setData datr "text/html" "")
                                                              (drag-start e ident)))
                                       :onDragEnd   (fn [e] (let [ident (om/get-ident this)]))
                                       :onDragOver  (fn [e] (let [ident (om/get-ident this)]
                                                              (.preventDefault e)
                                                              (-> e .-dataTransfer .-dropEffect (set! "move"))))
                                       :onDrop      (fn [e] (let [ident (om/get-ident this)]
                                                              (drag-drop e ident)))}
                                  (dom/h3 nil str title)
                                  (dom/a #js {:href (str "http://www.imdb.com/title/" imdbID)} "imdb")
                                  (dom/label nil "rating:") (dom/span nil imdbRating))))))

(def episode-ui (om/factory Episode {:keyfn :episode}))

(defui Episodes
       static om/IQuery
       (query [this]
              [{:episodes (om/get-query Episode)}])
       Object
       (drag-start [this e key]
                   (println (str "start:" this " e:" e " k" key))
                   (om/update-state! this assoc :dragged-key key))

       (drag-end [this e]
                 (println (str "end:" this " e:" e))
                 (om/update-state! this dissoc :dragged-key))

       (drag-over [this e]
                  (.preventDefault e)
                  (println (str "over:" this " e:" e)))

       (drag-drop [this e key]
                  (let [from-key (:dragged-key (om/get-state this))
                        to-key key]
                    (.preventDefault e)
                    (println (str "drop:" this " e:" e " k" to-key " f" from-key))
                    (om/transact! this `[(episodes/move {:move [~from-key ~to-key]})])))
       (render [this]
               (let [{:keys [episodes]} (om/props this)]
                 (dom/div nil
                          (dom/ol nil
                                  (for [ep episodes]
                                    (episode-ui (om/computed ep {:drag-start (fn [e k] (.drag-start this e k))
                                                                 :drag-end   #(.drag-end this %)
                                                                 :drag-over  #(.drag-over this %)
                                                                 :drag-drop  (fn [e k] (.drag-drop this e k))}))))))))

(defmulti reading om/dispatch)

(defmethod reading :episodes
  [{:keys [state]} key _]
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
  {:action
   (fn [] (swap! state move-episode (first move) (second move)))})

(def reconciler
  (om/reconciler
    {:state     (atom {})
     :normalize false
     :remotes   [:remote-episodes]                          ;; vector remotes
     :parser    (om/parser {:read reading :mutate mutating})
     :send      (reconciler-send "/episodes.json")}))

(om/add-root! reconciler Episodes (gdom/getElement "ui"))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
  )