(ns nl.mediquest.zorgrank.specs.api.zorgrank-choice.response
  (:require
   [clojure.spec.alpha :as s]))

(s/def ::response-body-200 nil?)

(s/def ::response-body-40x string?)
