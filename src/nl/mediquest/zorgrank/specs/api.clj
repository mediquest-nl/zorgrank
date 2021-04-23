(ns nl.mediquest.zorgrank.specs.api
  "Contains spec records that are used in more than one route. Uses
  vanilla spec from `nl.mediquest.zorgrank.specs`.
  See also: https://www.metosin.fi/blog/clojure-spec-as-a-runtime-transformation-engine/"
  (:require
   [clojure.spec.alpha :as s]
   [nl.mediquest.zorgrank.config :as config]
   [nl.mediquest.zorgrank.specs :as specs]
   [spec-tools.core :as st])
  (:import (java.util UUID)))

(s/def ::date
  (st/spec
   {:spec ::specs/date
    :description "gebruik yyyy-MM-dd, yyyy-MM or yyyy"
    :swagger/example (specs/random-date-str -10)
    :reason "invalide datum"}))

(s/def ::nillable-date
  (st/spec
   {:spec ::specs/nillable-date
    :description "gebruik een lege string, yyyy-MM-dd, yyyy-MM or yyyy"
    :swagger/example (specs/random-date-str -10)
    :reason "invalide datum"}))

(s/def ::geboortedatum
  (st/spec
   {:spec (s/or :date ::specs/date
                :year ::specs/year)
    :swagger/example "1970-01-01"
    :reason "invalide geboortedatum"}))

(s/def ::geslacht
  (st/spec
   {:spec ::specs/gender
    :description specs/gender-description
    :swagger/example "F"
    :reason "invalide gender code"}))

(s/def ::postcode
  (st/spec
   {:spec ::specs/postcode
    :description "een Nederlands postcode patroon 'nnnnAA'"
    :swagger/example "3581KP"
    :reason "invalide postcode"}))

(s/def ::specialisme-code
  (st/spec
   {:spec ::specs/specialisme-code
    :description "kies een valide specialisme-code"
    :swagger/example "0303"
    :reason "invalide specialisme code"}))

(s/def ::specialisme-codestelsel
  (st/spec
   {:spec ::specs/specialisme-codestelsel
    :description "kies een valide specialisme code (`COD16-VEKT`, `NON-VEKTIS`)"
    :swagger/example "COD016-VEKT"
    :reason "invalide specialisme-codestelsel"}))

(s/def ::specialisme
  (s/keys :req-un [::specialisme-codestelsel
                   ::specialisme-code]))

(s/def ::uuid-string
  (st/spec
   {:spec ::specs/uuid-string
    :description "kies een valide uuid string"
    :swagger/example (str (UUID/randomUUID))
    :reason "invalide uuid string"}))

(s/def ::uuid
  (st/spec
   {:spec uuid?
    :description "een valide uuid"
    :swagger/example (UUID/randomUUID)
    :reason "invalide uuid"}))

(s/def ::emailadres
  (st/spec
   {:spec (s/nilable string?)
    :description "emailadres"
    :swagger/example "piet@example.com"
    :reason "invalide waarde voor emailadres"}))

(s/def ::huisnummer
  (st/spec
   {:spec (s/nilable pos-int?)
    :description "huisnummer"
    :swagger/example 1
    :reason "invalide waarde voor huisnummer"}))

(s/def ::huisnummertoevoeging
  (st/spec
   {:spec (s/nilable string?)
    :description "huisnummertoevoeging"
    :swagger/example "A"
    :reason "invalide waarde voor huisnummertoevoeging"}))

(s/def ::organisatie-locatie
  (st/spec
   {:spec (s/nilable string?)
    :description "naam van de zorgaanbieder"
    :swagger/example "OLVG, locatie Oost"
    :reason "invalide waarde voor organisatie-locatie"}))

(s/def ::telefoonnummer
  (st/spec
   {:spec (s/nilable string?)
    :description "telefoonnummer"
    :swagger/example "06-12345678"
    :reason "invalide waarde voor telefoonnummer"}))

(s/def ::straat
  (st/spec
   {:spec (s/nilable string?)
    :description "straatnaam"
    :swagger/example "Burgemeester Reigerstraat"
    :reason "invalide waarde voor straatnaam"}))

(s/def ::woonplaats
  (st/spec
   {:spec (s/nilable string?)
    :description "een string voor de woonplaats"
    :swagger/example "Utrecht"
    :reason "invalide waarde voor woonplaats"}))

(s/def ::website
  (st/spec
   {:spec (s/nilable string?)
    :description "website"
    :swagger/example "www.example.com"
    :reason "invalide waarde voor website"}))

(def zorgcontractering-score-description
  (->> config/zorgcontracteringscore-descriptions
       vals
       (map :long)
       set))

(def zorgcontractering-score-title
  (->> config/zorgcontracteringscore-descriptions
       vals
       (map :short)
       set))

(def zorgcontractering-scores
  (->> config/zorgcontracteringscore-descriptions
       keys
       set))

(s/def ::zorgcontractering-score zorgcontractering-scores)
