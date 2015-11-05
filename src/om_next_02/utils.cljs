(ns om-next-02.utils
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :refer [chan put!]]
            [cognitect.transit :as t])
  (:import [goog.net XhrIo]))

(defn reconciler-send [url]
  "result takes a callback to receive json response"
  (fn [m cb]
    (.send XhrIo url
           (fn [_]
             (this-as this
               (let [x (t/read (t/reader :json) (.getResponseText this))]
                      (cb x)))))))