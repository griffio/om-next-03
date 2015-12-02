(ns om-next-03.cards
  (:require-macros
    [cljs.test :refer [is]]
    [devcards.core :as dc :refer [defcard defcard-doc noframe-doc deftest dom-node]])
  (:require
    [om-next-03.core :refer [drag-episode]]))

(dc/deftest must-drag-episode-from-first-to-last
            "returns a vector with the episodes in new ordering"
            (let [ eps [{:ep 1} {:ep 2} {:ep 3}]]
              (is (= (drag-episode eps 0 2) [{:ep 2} {:ep 3} {:ep 1}]))))
