(ns nl.mediquest.zorgrank.integration.core
  (:require
   [clojure.test :refer [is are testing use-fixtures deftest]]
   [nl.mediquest.zorgrank.integration.util :as util]))

;; NOTE: :once does not work with test-selectors like `^:integration`
(use-fixtures :each util/each-fixture)

(def specialists-0320
  ["St. Antonius Ziekenhuis, loc. Utrecht"
   "St. Antonius Ziekenhuis, loc. Nieuwegein"
   "Meander Medisch Centrum, loc. Amersfoort"
   "UMC Utrecht, loc. AZU"
   "St. Antonius Ziekenhuis, polikliniek Houten"])

(def reisafstand-and-score-spec-0320-pc-3581KP
  [[7 255.5]
   [10 232.2]
   [2 205.9]
   [4 178.9]
   [1 174.3]])

(def get-reisafstand-and-score
  (juxt (comp int :reisafstand) :score))

(deftest ^:integration simple-integration-test
  (testing "without postal code"
    (let [result (->> "api/v3/zorgrank-request"
                      util/post
                      :body
                      :data
                      (mapv :organisatie_locatie))]
      (is (= specialists-0320 result))))
  (testing "with postal code"
    (let [result (->> "api/v3/zorgrank-request"
                      (util/post {:patient {:postcode "3581KP"}})
                      :body
                      :data
                      (mapv get-reisafstand-and-score))]
      (is (= reisafstand-and-score-spec-0320-pc-3581KP result))))
  (testing "with uzovi and pakketcode"
    (let [result (->> "api/v3/zorgrank-request"
                      (util/post {:patient {:zorgverzekering {:uzovi "3311"
                                                              :pakketcode "107"}}})
                      :body
                      :data
                      (mapv :zorgcontractering))]
      ;; Removed from this version of API
      (is (= [nil nil nil nil nil] result))))
  (testing "with filter contractering = contractering"
    (let [result (->> "api/v3/zorgrank-request"
                      (util/post {:patient {:zorgverzekering {:uzovi "3311"
                                                              :pakketcode "107"}}
                                  :voorkeuren {:contractering {:contractering-filter [3]}}})
                      :body
                      :data
                      (mapv :zorgcontractering))]
      ;; NOTE: removed from this version of API
      (is (zero? (count result)))))
  (testing "with filter contractering and no zorgverzekering given in request"
    (let [result (->> "api/v3/zorgrank-request"
                      (util/post {:voorkeuren {:contractering {:contractering-filter [3]}}})
                      :body
                      :data
                      (mapv :zorgcontractering))]
      (is (empty? result))))
  (testing "icpc construction"
    (let [result (->> "api/v3/zorgrank-request"
                      (util/post {:verwijzing {:specialisme {:specialisme-codestelsel "COD016-VEKT"
                                                             :specialisme-code "0305"}
                                               :probleem {:probleem-naamcode "D89.00"
                                                          :probleem-codestelsel "icpc-1 NL"}}
                                  :patient {:postcode "3581KP"}})
                      :body
                      :data)]
      (are [expected actual] (= expected actual)
        ;; Complete structure
        5 (count result)
        ;; HTO
        ;; (geen aandoening mogelijk en koppelen op aangevraagd specialisme (long) en niet
        ;; het door mq gekoppelde specialisme orthopedie)
        "Medisch specialisten, orthopedie" (-> result first :hto :code_naam)
        "Medisch specialisten, orthopedie" (-> result (nth 2) :hto :code_naam)
        ;; Wachttijd
        ;; aandoening niet gevuld. Voor Zorgaanbieder 1 = aandoening
        ;; Wachttijd_toegangstijd voor Zorgaanbieder 2 = specialisme want wel gevuld
        "Hernia inguinalis" (-> result first :wachttijd_toegangstijd :code_naam)
        "Medisch specialisten, orthopedie" (-> result (nth 2) :wachttijd_toegangstijd :code_naam)
        ;;KWIC
        ;; De regel voor KWIC is dat als er minimaal 1 zorgaanbieder is met een score op
        ;; aandoeningsniveau, dat we dan alleen scores van de aandoening meenemen.
        "Hernia inguinalis" (-> result first :kwic_totaalscore :code_naam)))))
