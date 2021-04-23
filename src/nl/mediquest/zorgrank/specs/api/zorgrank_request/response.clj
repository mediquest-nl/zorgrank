(ns nl.mediquest.zorgrank.specs.api.zorgrank-request.response
  (:require
   [clojure.spec.alpha :as s]
   [nl.mediquest.zorgrank.specs :as specs]
   [nl.mediquest.zorgrank.specs.api :as specs.api]
   [spec-tools.core :as st]))

(s/def ::mq-kwaliteit-score
  (st/spec
   {:spec (s/nilable number?)
    :swagger/example 80.0
    :description (str "Kwaliteitsscore tussen 0 en 100 gebaseerd op de KWIC-score, de HTO-score en "
                      "de patientervaringsscore.")
    :reason "geen getal"}))

(s/def ::rank
  (st/spec
   {:spec pos-int?
    :description "een positieve integer voor ranking"
    :swagger/example 1
    :reason "invalide ranking"}))

(s/def ::reisafstand
  (st/spec
   {:spec (s/nilable number?)
    :description "Reisafstand in kilometers"
    :swagger/example 1.0
    :reason "geen getal"}))

(s/def ::score
  (st/spec
   {:spec number?
    :description "een getal tussen de 0 en 10000"
    :swagger/example 5000
    :reason "geen getal tussen de 0 en 10000"}))

(s/def :wachttijd/score
  (st/spec
   {:spec nat-int?
    :description "wachttijd in weken"
    :swagger/example 1
    :reason "geen getal van 0 of hoger"}))

(s/def :kwic-totaalscore/score
  (st/spec
   {:spec (partial specs/number-in-range? -2 3)
    :description (str "Waarde voor kwic-totaalscore. Waarbij KWIC de gebruikte methodiek is om "
                      "losse indicatoren uit verschillende bronnen te vertalen in "
                      "kwaliteitsoordelen. Op basis van de indicatoren en de weging wordt er score "
                      "tussen -2 en 2 punten vastgesteld.")
    :swagger/example 1
    :reason "geen getal tussen de -2 en 2 (inclusief)"}))

(s/def :kwic-sterscore/score
  (st/spec
   {:spec (partial specs/number-in-range? 1 6)
    :description (str "Waarde voor de kwic-sterscore. Waarbij de KWIC sterrenscores wordten "
                      "vastgesteld op basis van de KWIC totaalscore.\n"
                      "waardes: 1 ster (matig), 2 sterren (voldoende), 3 sterren (goed), 4 sterren ("
                      "zeer goed), 5 sterren (uitstekend)")
    :swagger/example 1
    :reason "geen getal tussen de 1 en 5 (inclusief)"}))

(s/def :hto/score
  (st/spec
   {:spec (partial specs/number-in-range? 1 5)
    :description "gemiddelde score tussen 1 en 4 uit het huisartsentevredenheidsonderzoek"
    :swagger/example 1
    :reason "geen getal tussen de 1 en 4 (inclusief)"}))

(s/def ::type
  (st/spec
   {:spec (s/and string? #{"specialisme" "aandoening"})
    :description "of score op niveau is van de aandoening of het specialisme"
    :swagger/example "specialisme"
    :reason "invalide type"}))

(s/def ::code
  (st/spec
   {:spec (s/or :probleem-naamcode ::specs/probleem-naamcodes :specialisme-code ::specs/specialisme-code)
    :description (str "hier staat een vektis of icpc1 code die aangeeft waar "
                      "de score over gaat")
    :swagger/example "0301"
    :reason "invalide code voor probleem of specialisme"}))

(s/def ::code-naam
  (st/spec
   {:spec (s/nilable string?)
    :description (str "hier staat de icpc beschrijving van de aandoening of de vektis "
                      "beschrijving van het specialisme waar de score over gaat, bijv "
                      "carpaal tunnel syndroom")
    :swagger/example "Medisch specialisten, oogheelkunde"
    :reason "invalide beschrijving"}))

(s/def ::code-naam-synoniem
  (st/spec
   {:spec (s/nilable string?)
    :description (str "hier staat een korte, begrijpelijke beschrijving van de aandoening "
                      "/specialisme waar de score over gaat, bijv Spataderen of Oogheelkunde")
    :swagger/example "Cardiologie"
    :reason "invalide beschrijving"}))

(s/def ::aantal
  (st/spec
   {:spec pos-int?
    :description "op hoeveel waarnemingen of respondenten de score gebaseerd is"
    :swagger/example "10"
    :reason "invalide aantal"}))

(s/def ::wachttijd-behandeltijd
  (s/nilable
   (s/keys :req-un [:wachttijd/score
                    ::type
                    ::code
                    ::code-naam
                    ::code-naam-synoniem])))

(s/def ::wachttijd-toegangstijd
  (s/nilable
   (s/keys :req-un [:wachttijd/score
                    ::type
                    ::code
                    ::code-naam
                    ::code-naam-synoniem])))

(s/def ::kwic-totaalscore
  (s/nilable
   (s/keys :req-un [:kwic-totaalscore/score
                    ::type
                    ::code
                    ::code-naam
                    ::code-naam-synoniem])))

(s/def ::kwic-sterscore
  (s/nilable
   (s/keys :req-un [:kwic-sterscore/score
                    ::type
                    ::code
                    ::code-naam
                    ::code-naam-synoniem])))

(s/def ::hto
  (s/nilable
   (s/keys :req-un [:hto/score
                    ::type
                    ::code
                    ::code-naam
                    ::code-naam-synoniem
                    ::aantal])))

(s/def :zorgcontractering/score
  (st/spec
   {:spec ::specs.api/zorgcontractering-score
    :description (str "Code die aangeeft in welke mate de aandoening of het specialisme "
                      "gecontracteerd is door de zorgverzekeraar van de patiënt. Mogelijke codes: "
                      "0, 1, 2, 3, 4.")
    :swagger/example 3
    :reason "geen score tussen 0 en 4"}))

(s/def :zorgcontractering/score-beschrijving
  (st/spec
   {:spec specs.api/zorgcontractering-score-description
    :description "Omschrijving van score"
    :swagger/example "Hier staat wat de zorgcontracteringsscore betekent"
    :reason "Geen geldige beschrijving van score"}))

(s/def :zorgcontractering/score-titel
  (st/spec
   {:spec specs.api/zorgcontractering-score-title
    :description "Hier staat een korte, begrijpelijke beschrijving van wat de zorgcontracteringsscore betekent"
    :swagger/example "Volledig"
    :reason "Geen geldige titel van score"}))

(s/def ::zorgcontractering
  (s/nilable
   (s/keys :req-un [:zorgcontractering/score
                    :zorgcontractering/score-beschrijving
                    :zorgcontractering/score-titel
                    ::type
                    ::code
                    ::code-naam
                    ::code-naam-synoniem])))

(s/def :patientervaring/score
  (st/spec
   {:spec (partial specs/number-in-range? 1 11)
    :description "gemiddelde score tussen 1 en 10 gegeven door patiënten"
    :swagger/example 8.1
    :reason "geen score tussen 1 en 10"}))

(s/def ::patientervaring
  (s/nilable
   (s/keys :req-un [:patientervaring/score
                    ::type
                    ::code
                    ::code-naam
                    ::code-naam-synoniem
                    ::aantal])))

(s/def ::specialist-row
  (s/keys :req-un [::specs/zorgaanbiederidentificatienummer
                   ::specs/zorgaanbiederidentificatienummer-codestelsel
                   ::specs.api/organisatie-locatie
                   ::specs.api/straat
                   ::specs.api/huisnummer
                   ::specs.api/huisnummertoevoeging
                   ::mq-kwaliteit-score
                   ::specs.api/organisatie-locatie
                   ::specs.api/postcode
                   ::specs.api/woonplaats
                   ::specs.api/telefoonnummer
                   ::specs.api/emailadres
                   ::specs.api/website
                   ::specs.api/woonplaats
                   ::reisafstand
                   ::zorgcontractering
                   ::hto
                   ::rank
                   ::score
                   ::kwic-sterscore
                   ::kwic-totaalscore
                   ::patientervaring
                   ::wachttijd-toegangstijd
                   ::wachttijd-behandeltijd]))

(s/def ::data
  (s/coll-of ::specialist-row))

(s/def ::zorgrank-id ::specs.api/uuid)

(s/def ::response-body
  (s/keys :req-un [::data
                   ::zorgrank-id]))

(def response-keys
  (mapv (comp keyword name) (nth (s/describe ::specialist-row) 2)))

(defn select-response-keys
  [row]
  (select-keys row response-keys))
