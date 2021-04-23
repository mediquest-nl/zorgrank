(ns nl.mediquest.zorgrank.specs
  "Contains vanilla specs. See namespaces in `nl.mediquest.zorgrank.specs` for
  spec records used in routers."
  (:require
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as gen]
   [clojure.string :as string]
   [nl.mediquest.zorgrank.util :refer [file->edn]]
   [spec-tools.core :as st])
  (:import
   (java.lang IllegalArgumentException)
   (java.time LocalDate)
   (java.time.format DateTimeFormatter DateTimeParseException)
   (java.util UUID)))

(defn number-in-range?
  "Return true if start <= val, val < end and val is a Number."
  [start end val]
  (and (number? val) (<= start val) (< val end)))

(defmacro number-in
  "Returns a spec that validates a Number in the
  range from start (inclusive) to end (exclusive)."
  [start end]
  `(s/spec (and number? #(number-in-range? ~start ~end %))
           :gen #(gen/double* {:min ~start :max (dec ~end)})))

(defn valid-uuid-string?
  [^String uuid-string]
  (try (UUID/fromString uuid-string)
       true
       (catch IllegalArgumentException _ false)))

(s/def ::uuid-string
  (s/with-gen
    (s/and string? valid-uuid-string?)
    #(gen/fmap str (gen/uuid))))

(s/def ::uuid-keyword
  (s/with-gen
    (s/and keyword? (comp valid-uuid-string? name))
    #(gen/fmap (comp keyword str) (gen/uuid))))

(s/def ::clients
  (s/with-gen
    (s/nilable
     (s/and
      map?
      (s/every-kv
       ::uuid-keyword
       string?)
      (comp distinct? vals)))
    #(gen/map (s/gen ::uuid-keyword)
              (gen/string-alphanumeric))))

(def date-time-formatter (DateTimeFormatter/ofPattern "yyyy-MM-dd"))

(defn valid-yyyy-MM-dd?
  [date]
  (try (LocalDate/parse date date-time-formatter)
       true
       (catch DateTimeParseException _
         false)))

(defn random-date-str [n]
  (.. (LocalDate/now) (plusDays (rand-int n))
      (format date-time-formatter)))

(s/def ::date
  (s/with-gen
    (s/and string? valid-yyyy-MM-dd?)
    #(gen/fmap (comp random-date-str -) (gen/int))))

(s/def ::nillable-date
  (s/with-gen
    (s/nilable (s/and string? #(or (string/blank? %) (valid-yyyy-MM-dd? %))))
    #(gen/fmap (comp random-date-str -) (gen/int))))

(s/def ::year
  (s/with-gen
    (s/and string? #(re-matches #"[1-2](0|9)[0-9]{2}" %))
    #(gen/return (str (+ 1919 (rand-int 100))))))

(s/def ::postcode
  (let [postcode-regex #"[1-9][0-9]{3} ?[a-zA-Z]{0,2}"]
    (s/with-gen
      (s/and string? #(re-matches postcode-regex %))
      #(gen/fmap
        (partial apply str)
        (let [letters (gen/elements (->>
                                     (concat (range 65 91)  (range 97 123))
                                     (map char)))
              numbers (into [(gen/choose 1 9)] (repeat 3 (gen/choose 0 9)))]
          (gen/one-of
           [(apply gen/tuple (conj numbers letters letters))
            (apply gen/tuple (conj numbers letters))
            (apply gen/tuple numbers)]))))))

(s/def ::zorgaanbiederidentificatienummer-codestelsel
  (st/spec
   {:spec #{"MQ" "AGB"}
    :description "kies een valide zorgaanbiederidentificatienummer-codestelsel (`MQ`, `AGB`)"
    :swagger/example "AGB"
    :reason "invalide zorgaanbiederidentificatienummer-codestelsel"}))

(defn code-generator
  [n]
  (gen/fmap
   (partial apply str)
   (let [i (gen/choose 0 9)]
     (apply gen/tuple (repeat n i)))))

(s/def ::mq-bhc-id
  (let [max-mq-bhc-id 3000000]
    (s/int-in 0 max-mq-bhc-id)))

(s/def ::agb
  (let [referrer-code-regex #"[0-9]{8}"]
    (s/with-gen
      (s/and string? #(re-matches referrer-code-regex %))
      #(code-generator 8))))

(s/def ::zorgaanbiederidentificatienummer
  (st/spec
   {:spec (s/or
           :mq ::mq-bhc-id
           :agb ::agb)
    :description "gebruik een valide MQ of AGB code"
    :swagger/example "01057825"
    :reason "invalide MQ of AGB code"}))

(s/def ::zorgaanbieder
  (s/keys :req-un [::zorgaanbiederidentificatienummer
                   ::zorgaanbiederidentificatienummer-codestelsel]))

(s/def ::specialisme-code
  (let [specialism-regex #"[0-9]{4}|[a-z]{3}"]
    (s/with-gen
      (s/and string? #(re-matches specialism-regex %))
      #(code-generator 4))))

(s/def ::specialisme-codestelsel
  (st/spec
   {:spec (let [codes #{"COD016-VEKT" "NON-VEKTIS"}]
            (s/with-gen
              (fn [x]
                (codes
                 (string/upper-case x)))
              #(gen/return (rand-nth (into [] codes)))))}))

(s/def ::specialisme
  (s/keys :req-un [::specialisme-codestelsel
                   ::specialisme-code]))

(s/def ::score number?)

(s/def ::weight
  (st/spec
   {:spec (let [max 100]
            (s/int-in 1 (inc max)))
    :description "kies een integer tussen de 1 en 100 (default 10)"
    :swagger/example 10
    :reason "invalide weight"}))

(s/def ::afstand-weight (s/nilable ::weight))
(s/def ::kwaliteit-weight (s/nilable ::weight))
(s/def ::wachttijd-weight (s/nilable ::weight))

(s/def ::user-weights
  (s/keys :req-un [::afstand-weight
                   ::kwaliteit-weight
                   ::wachttijd-weight]))

(s/def ::quality-filter
  (let [max-value 0]
    (s/int-in 0 (inc max-value))))

(s/def ::sort-range
  (let [max-value 3]
    (s/int-in 1 (inc max-value))))

(s/def ::score number?)
(s/def ::distance (number-in 0 1000))

(s/def ::best-on-top
  (st/spec
   {:spec boolean?
    :description "best_on_top is altijd van best naar slechtst"
    :swagger/example true
    :reason "gebruik een boolean"}))

(s/def ::normalized-value (s/int-in 0 101))
(s/def ::bhc-id pos-int?)
(s/def ::bhc-name string?)
(s/def ::kwaliteit-hto (s/int-in 0 101))
(s/def ::kwaliteit-kwic-specialty (s/int-in 0 101))
(s/def ::kwaliteit-kwic-transparency (s/int-in 0 101))
(s/def ::reisafstand-norm (s/nilable (s/and number? #(<= 0 % 100))))
(s/def ::mq-kwaliteit-norm (s/nilable (s/and number? #(<= 0 % 100))))
(s/def ::wachttijd-toegangstijd-norm (s/nilable (s/and number? #(<= 0 % 100))))

(s/def ::pc-4 (s/int-in 0 10000))

(def gender-set
  #{"F" "M" "eUN" "UNK"
    "f" "m" "un" "unk"
    "male" "female" "Male" "Female"
    "Other" "other" "Unknown" "unknown"})

(s/def ::gender
  gender-set)

(def gender-description
  (->> gender-set
       (map (fn [s] (format "`%s`" s)))
       (interpose ", ")
       (apply str)
       (format "Kies een valide gender code (%s)")))

(s/def ::normalized-row
  (s/keys :opt-un [::mq-kwaliteit-norm
                   ::wachttijd-toegangstijd-norm
                   ::reisafstand-norm]))

(def icpc->probleem-naamcode-set
  (comp set keys))

(def icpc
  (file->edn "edn/icpc-codes.edn"))

(s/def ::probleem-naamcodes
  (icpc->probleem-naamcode-set icpc))

(s/def ::probleem-naamcode
  (st/spec
   {:spec ::probleem-naamcodes
    :description "kies een valide code in overeenstemming met de codelijst"
    :swagger/example "N93.00"
    :reason "invalide probleem-naamcode"}))

(s/def :database/datamart
  #{"zorgrank_datamart"
    "zorgrank_datamart_aandoeningen"})
