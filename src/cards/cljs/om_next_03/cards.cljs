(ns om-next-03.cards
  (:require-macros
    [cljs.test :refer [is]]
    [devcards.core :as dc :refer [defcard defcard-doc noframe-doc deftest dom-node]])
  (:require
    [om-next-03.core :refer [drag-episode episodes-ui]]
    [om.next :as om]
    [om.dom :as dom]))

(deftest dragging-episode-from-first-to-last-position
            "returns a vector with the episodes in new ordering"
            (let [eps [{:ep 1} {:ep 2} {:ep 3}]]
              (is (= (drag-episode eps 0 2) [{:ep 2} {:ep 3} {:ep 1}]))))

(deftest dragging-episode-from-last-to-first-position
            "returns a vector with the episodes in new ordering"
            (let [eps [{:ep 1} {:ep 2} {:ep 3}]]
              (is (= (drag-episode eps 2 0) [{:ep 3} {:ep 1} {:ep 2}]))))

(deftest single-episode
            "returns a vector with single episode"
            (let [eps [{:ep 1}]]
              (is (= (drag-episode eps 0 0) eps))))

(defcard
  "### Episodes"
  (fn [state _]
    (dom/div nil
             (episodes-ui @state)))
  {:episodes [{:episode 1 :title "Title A" :released "2012-01-13" :imdbRating 7.8  :imdbID "tt1668746"}
              {:episode 2 :title "Title B" :released "2012-02-13" :imdbRating 7.6  :imdbID "tt1668746"}]}
  {:inspect-data true :history false})