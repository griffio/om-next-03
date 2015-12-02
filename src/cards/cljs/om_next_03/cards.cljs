(ns om-next-03.cards
  (:require-macros
    [cljs.test :refer [is]]
    [devcards.core :as dc :refer [defcard defcard-doc noframe-doc deftest dom-node]])
  (:require
    [om-next-03.core :refer [drag-episode]]))

(dc/deftest dragging-episode-from-first-to-last-position
            "returns a vector with the episodes in new ordering"
            (let [ eps [{:ep 1} {:ep 2} {:ep 3}]]
              (is (= (drag-episode eps 0 2) [{:ep 2} {:ep 3} {:ep 1}]))))

(dc/deftest dragging-episode-from-last-to-first-position
            "returns a vector with the episodes in new ordering"
            (let [ eps [{:ep 1} {:ep 2} {:ep 3}]]
              (is (= (drag-episode eps 2 0) [{:ep 3} {:ep 1} {:ep 2}]))))

(dc/deftest single-episode
            "returns a vector with single episode"
            (let [ eps [{:ep 1}]]
              (is (= (drag-episode eps 0 0) eps))))
