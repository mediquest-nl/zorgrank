(ns nl.mediquest.zorgrank.specs.api.zorgrank-request.request
  (:require
   [clojure.spec.alpha :as s]
   [nl.mediquest.zorgrank.config :as config]
   [nl.mediquest.zorgrank.specs :as specs]
   [nl.mediquest.zorgrank.specs.api :as specs.api]
   [spec-tools.core :as st]))

(s/def ::uzovi
  (let [uzovi-codes-regex #"\d{4}"]
    (st/spec
     {:spec (s/with-gen (s/and string? #(re-matches uzovi-codes-regex %))
              #(specs/code-generator 4))
      :description "kies een valide uzovi code"
      :swagger/example "3311"
      :reason "invalide uzovi code"})))

(s/def ::labelcode
  (st/spec
   {:spec string?
    :description "kies een valide cov (controle op verzekeringsrecht) labelcode"
    :swagger/example "ZKA"
    :reason "invalide cov labelcode"}))

(s/def ::labelnaam
  (st/spec
   {:spec string?
    :description "kies een valide cov (controle op verzekeringsrecht) labelnaam"
    :swagger/example "Zilveren Kruis"
    :reason "invalide labelnaam"}))

(s/def ::pakketcode
  (st/spec
   {:spec string?
    :description "kies een valid cov (controle op verzekeringsrecht) pakketcode"
    :swagger/example "106"
    :reason "invalide cov pakketcode"}))

(s/def ::pakketnaam
  (st/spec
   {:spec string?
    :description "kies een valid cov (controle op verzekeringsrecht) pakketnaam"
    :swagger/example "Basis Budget"
    :reason "invalide pakketnaam"}))

(s/def ::verzekeringssoort
  (st/spec
   {:spec #{"B", "A", "BZ", "H", "T", "AT"}
    :description "kies een valide verzekeringssoortcode (https://zibs.nl/wiki/Betaler-v3.1(2018NL)#VerzekeringssoortCodelijst)"
    :swagger/example "B"
    :reason "invalide verzekeringssoortcode"}))

(s/def ::zorgverzekering
  (s/keys :opt-un [::labelcode
                   ::labelnaam
                   ::pakketcode
                   ::pakketnaam
                   ::verzekeringssoort]
          :req-un [::uzovi]))

(s/def ::pakketcode-and-uzovi
  (s/keys :req-un [::pakketcode
                   ::uzovi]))
(s/def ::patient
  (s/keys :opt-un [::specs.api/geslacht
                   ::specs.api/postcode
                   ::specs.api/geboortedatum
                   ::zorgverzekering]))

(s/def ::datum-verwijzing
  ::specs.api/date)

(s/def ::probleem-codestelsel
  (st/spec
   {:spec #{"icpc-1 NL"}
    :description "kies een valide probleem-codestelsel"
    :swagger/example "icpc-1 NL"
    :reason "invalide probleem-codestelsel"}))

(s/def ::probleem
  (s/keys :opt-un [::specs/probleem-naamcode
                   ::probleem-codestelsel]))

(s/def ::verwijzer
  ::specs/zorgaanbieder)


(s/def ::zorgaanbieder
  ::specs/zorgaanbieder)

;; Differs from main specs

(s/def ::zorgaanbiederidentificatienummer-codestelsel
  (st/spec
   {:spec #{"MQ" "AGB"}
    :description "kies een valide zorgaanbiederidentificatienummer-codestelsel (`MQ`, `AGB`)"
    :swagger/example "MQ"
    :reason "invalide zorgaanbiederidentificatienummer-codestelsel"}))

(s/def ::zorgaanbiederidentificatienummer
  (st/spec
   {:spec (s/or
           :mq ::specs/mq-bhc-id
           :agb ::specs/agb)
    :description "gebruik een valide MQ of AGB code"
    :swagger/example 40400
    :reason "invalide MQ of AGB code"}))

(s/def ::zorgaanbieder
  (s/keys :req-un [::zorgaanbiederidentificatienummer
                   ::zorgaanbiederidentificatienummer-codestelsel]))

(s/def ::voorkeursaanbieder
  ::zorgaanbieder)

(s/def ::zorgsoort-code
  (st/spec
   {:spec #{"1_5" "2_0"}
    :description "kies een valide zorgsoort-code (`1_5` voor anderhalfdelijns- en `2_0` voor tweedelijnszorg)"
    :swagger/example "2_0"
    :reason "invalide zorgsoort-code (`1_5` voor anderhalfdelijns- en `2_0` voor tweedelijnszorg)"}))

(s/def ::urgentie
  (st/spec
   {:spec #{"regulier" "urgent"}
    :description "kies een valide urgentie (`regulier` of `urgent`)"
    :swagger/example "regulier"
    :reason "invalide urgentie"}))

(s/def ::verwijzing
  (s/keys :opt-un [::verwijzer
                   ::voorkeursaanbieder
                   ::probleem
                   ::datum-verwijzing
                   ::zorgsoort-code
                   ::urgentie]
          :req-un [::specs.api/specialisme]))

(s/def ::pref-referrer
  (st/spec
   {:spec string?
    :description "omschrijving van geprefereerde zorgaanbieder"
    :swagger/example "Maasstad ziekenhuis, afd. Dermatologie"
    :reason "invalide omschrijving van geprefereerde zorgaanbieder"}))

(s/def ::pref-referrer-grp
  (s/keys :opt-un [::pref-referrer
                   ::specs/weight
                   ::filter]))

(s/def ::pref-gender-health-professional
  ::specs.api/geslacht)

(s/def ::pref-gender-health-professional-grp
  (s/keys :opt-un [::pref-gender-health-professional
                   ::specs/weight
                   ::filter]))

(s/def ::kwaliteit-filter
  (st/spec
   {:spec (s/int-in 0 (inc 100))
    :description "kies een integer tussen de 0 en 100"
    :swagger/example 50
    :reason "invalide kwaliteit-filter"}))

(s/def ::kwaliteit-weight
  ::specs/weight)

(s/def ::kwaliteit
  (s/keys :opt-un [::kwaliteit-filter
                   ::kwaliteit-weight]))

(s/def ::pref-language
  (let [country-set config/languages]
    (st/spec
     {:spec country-set
      :description "Gebruik ISO 3166 Country Codes met FHIR"
      :swagger/example "NL"
      :reason "invalide landcode"})))

(s/def ::pref-language-grp
  (s/keys :opt-un [::pref-language
                   ::specs/weight
                   ::filter]))

(s/def ::afstand-filter
  (let [max-distance 400]
    (st/spec
     {:spec (s/int-in 0 (inc max-distance))
      :description "kies een maximale afstand (max 400 km)"
      :swagger/example 400
      :reason "invalide afstand"})))

(s/def ::afstand-weight ::specs/weight)

(s/def ::afstand
  (s/keys :opt-un [::afstand-filter
                   ::afstand-weight]))

(s/def ::wachttijd-filter
  (let [max-waiting-time 200]
    (st/spec
     {:spec (s/int-in 0 (inc max-waiting-time))
      :description "kies een maximale wachttijd in weken, max 200 weken"
      :swagger/example 200
      :reason "invalide wachttijd"})))

(s/def ::wachttijd-weight ::specs/weight)

(s/def ::wachttijd
  (s/keys :opt-un [::wachttijd-filter
                   ::wachttijd-weight]))

(s/def ::contractering-filter
  (st/spec
   {:spec (s/coll-of ::specs.api/zorgcontractering-score :kind vector? :distinct true)
    :description "kies een of meerdere valide contracteringsvariabeles tussen de 0 en 4"
    :swagger/example []
    :reason "invalide array met contracteringvariabelen"}))

(s/def ::contractering
  (s/keys :opt-un [::contractering-filter]))

(s/def ::number-results
  (st/spec
   {:spec pos-int?
    :description "retourneert de top `n` van de ranking"
    :swagger/example 5
    :reason "geen positieve integer"}))

(s/def ::sorteren
  (st/spec
   {:spec #{"wachttijd" "afstand" "kwaliteit"}
    :description "kies voorkeur waarop gesorteerd kan worden"
    :swagger/example "wachttijd"
    :reason "geen geldige voorkeur waarop gesorteerd kan worden"}))

(s/def ::voorkeuren
  (s/keys :opt-un [::wachttijd
                   ::afstand
                   ::kwaliteit
                   ::sorteren
                   ::contractering
                   ::number-results]))

(s/def ::zorgrank-request
  (s/keys :opt-un [::patient
                   ::voorkeuren]
          :req-un [::verwijzing]))
