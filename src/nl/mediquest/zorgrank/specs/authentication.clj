(ns nl.mediquest.zorgrank.specs.authentication
  (:require
   [clojure.spec.alpha :as s]
   [nl.mediquest.zorgrank.specs :as specs]))

(s/def ::client-key ::specs/uuid-keyword)
(s/def ::authenticated boolean?)
(s/def ::organization string?)

(s/def ::authenticated-request
  (s/nilable
   (s/keys :req-un [::client-key
                    ::authenticated
                    ::organization])))
