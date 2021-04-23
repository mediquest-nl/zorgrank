(ns nl.mediquest.zorgrank.specs.api.zorgrank-choice.request
  (:require
   [clojure.spec.alpha :as s]
   [nl.mediquest.zorgrank.specs.api :as specs.api]
   [spec-tools.core :as st]))

(s/def ::zorgrank-id ::specs.api/uuid-string)

(s/def ::path-params
  (s/keys :req-un [::zorgrank-id]))

(s/def ::gebruiker
  (let [patient  1
        tipp     2
        huisarts 3
        user-set #{patient tipp huisarts}]
    (st/spec
     {:spec user-set
      :description "een valide type gebruiker: 1 = patient, 2 = tipp, 3 = huisarts"
      :swagger/example 1
      :reason "invalide type gebruiker"})))

(s/def ::gekozen-zorgaanbieder
  (st/spec
   {:spec pos-int?
    :description "een positieve integer voor de positie"
    :swagger/example 1
    :reason "geen positieve integer"}))

(s/def ::zorgrank-choice
  (s/keys :req-un [::gebruiker
                   ::gekozen-zorgaanbieder]))
