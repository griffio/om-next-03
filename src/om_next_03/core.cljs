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

(defn move-episode [state from to])

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
               (let [{:keys [episode title imdbRating imdbID]} (om/props this)]
                 (dom/li nil
                         (dom/div #js {:data-id     episode
                                       :className   "js-dragging"
                                       :draggable   true
                                       :onDragStart (fn [e] (let [datr (.-dataTransfer e) ident (om/get-ident this)]
                                                              (set! (.-effectAllowed datr) "move")
                                                              (.setData datr "text/html" "")))
                                       :onDragOver  (fn [e] (let [targ (.-currentTarget e)]
                                                              (.preventDefault e)
                                                              (-> e .-dataTransfer .-dropEffect (set! "move"))))
                                       :onDrop      (fn [e] (let [target-ident (om/get-ident this) drag-ident (:drag-ident (om/get-state this))]))}

                                  (dom/h3 nil str title)
                                  (dom/a #js {:href (str "http://www.imdb.com/title/" imdbID)} "imdb")
                                  (dom/label nil "rating:") (dom/span nil imdbRating))))))

(def episode-ui (om/factory Episode {:keyfn :episode}))

(defui Episodes
       static om/IQuery
       (query [this]
              [{:episodes (om/get-query Episode)}])
       Object
       (render [this]
               (let [{:keys [episodes]} (om/props this)]
                 (dom/div nil
                          (apply dom/ol nil
                                 (map episode-ui episodes))))))

(defmulti reading om/dispatch)

(defmethod reading :episodes
  [{:keys [state]} key _]
  (let [st @state]
    (if (contains? st key)
      {:value (get st key)}                                 ;; loads data from app state
      {:remote true})))                                     ;; loads data from /episodes.json

(defmethod reading :episode/dragged
  [{:keys [state]} key _]
  (let [st @state]
    {:value (get st key)}))

(defmulti mutating om/dispatch)

(defmethod mutating 'episode/drag
  [{:keys [state]} _ params]
  {:value  [:episode/dragged]
   :action (fn []
             (if-not (empty? params)
               (swap! state assoc :episode/dragged params)
               (swap! state assoc :episode/dragged nil)))})

(defmethod mutating 'episodes/move
  [{:keys [state]} _ {:keys [from to]}]
  {:value  [:episodes]
   :action #(swap! state move-episode from to)})

(def reconciler
  (om/reconciler
    {:state     (atom {})
     :normalize false
     :parser    (om/parser {:read reading :mutate mutating})
     :send      (reconciler-send "/episodes.json")}))

(om/add-root! reconciler Episodes (gdom/getElement "ui"))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
  )