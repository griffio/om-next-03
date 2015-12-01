(ns om-next-03.cards
  (:require-macros
    [cljs.test :refer [is]]
    [devcards.core :as dvcs]
    )
  (:require [om.next :as om :refer-macros [defui]]
            [goog.dom :as gdom]
            [om.dom :as dom]
            [devcards.core :as dvcs]
            [om-next-03.core :as core]
            ))

(dvcs/deftest it-must-be-true
            "it must be true"
            (is (= true true)))
