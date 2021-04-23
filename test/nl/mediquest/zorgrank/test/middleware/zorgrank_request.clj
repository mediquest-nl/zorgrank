(ns nl.mediquest.zorgrank.test.middleware.zorgrank-request
  (:require
   [clojure.spec.test.alpha :as stest]
   [clojure.test :refer [deftest are is testing use-fixtures]]
   [nl.mediquest.zorgrank.db.events :as events-db]
   [nl.mediquest.zorgrank.middleware.zorgrank-request :as zorgrank-request]))

(defn ns-fixture [tests]
  (stest/instrument)
  (tests))

(use-fixtures :once ns-fixture)

(defn get-results
  [specialist-rows voorkeuren]
  (->> ((zorgrank-request/wrap-calculate-algorithm identity)
        {:specialist-rows specialist-rows
         :body-params {:voorkeuren voorkeuren}})
       :data
       (mapv :bhc-id)))

(deftest test-wrap-calculate-algorithm-filtering
  (testing "Wrap Calculate Algorithm Filtering"
    (letfn [(count* [r] (-> r :data count))]
      ;; filter when reisafstand is nil or > 20
      (is (= 3 (count* ((zorgrank-request/wrap-calculate-algorithm identity)
                        {:specialist-rows [{:reisafstand 20}
                                           {:reisafstand 0}
                                           {:reisafstand 21}
                                           {:reisafstand nil}]
                         :body-params {:voorkeuren {:afstand {:afstand-filter 20}}}}))))
      ;; filter when wachttijd-toegangstijd is nil or > 20
      (is (= 2 (count* ((zorgrank-request/wrap-calculate-algorithm identity)
                        {:specialist-rows [{:wachttijd-toegangstijd-score 20}
                                           {:wachttijd-toegangstijd-score 0}
                                           {:wachttijd-toegangstijd-score 21}
                                           {:wachttijd-toegangstijd-score nil}]
                         :body-params {:voorkeuren {:wachttijd {:wachttijd-filter 20}}}}))))
      ;; filter wachttijd-toegangstijd and reisafstand
      (is (= 2 (count* ((zorgrank-request/wrap-calculate-algorithm identity)
                        {:specialist-rows [{:wachttijd-toegangstijd-score 20 :reisafstand 0}
                                           {:wachttijd-toegangstijd-score 0 :reisafstand 20}
                                           {:wachttijd-toegangstijd-score 21}
                                           {:wachttijd-toegangstijd-score nil :reisafstand 10}]
                         :body-params {:voorkeuren {:wachttijd {:wachttijd-filter 20}
                                                    :afstand {:afstand-filter 20}}}}))))
      ;; don't filter when filter is nil
      (is (= 1 (count* ((zorgrank-request/wrap-calculate-algorithm identity)
                        {:specialist-rows [{:wachttijd-toegangstijd 20 :reisafstand 0}]
                         :body-params {:voorkeuren {:wachttijd {:wachttijd-filter nil}
                                                    :afstand {:afstand-filter nil}}}}))))

      ;; filter contract-score >= 3
      (let [db-rows [{:contract-score 4}
                     {:contract-score 3}
                     {:contract-score 1}
                     {:no-contract-score nil}]
            expected-1 [{:contract-score 3, :score 0.0, :rank 1}]
            expected-2 [{:contract-score 4, :score 0.0, :rank 1}
                        {:contract-score 3, :score 0.0, :rank 2}]
            expected-3 [{:contract-score 4, :score 0.0, :rank 1}
                        {:contract-score 3, :score 0.0, :rank 2}
                        {:contract-score 1, :score 0.0, :rank 3}
                        {:no-contract-score nil, :score 0.0, :rank 4}]]
        (are [expected actual] (= expected actual)
          expected-1 ((zorgrank-request/wrap-calculate-algorithm :data)
                      {:specialist-rows db-rows
                       :body-params
                       {:voorkeuren
                        {:contractering
                         {:contractering-filter [3]}}}})
          expected-2 ((zorgrank-request/wrap-calculate-algorithm :data)
                      {:specialist-rows db-rows
                       :body-params
                       {:voorkeuren
                        {:contractering
                         {:contractering-filter [3 4]}}}})
          expected-3 ((zorgrank-request/wrap-calculate-algorithm :data)
                      {:specialist-rows db-rows
                       :body-params
                       {:voorkeuren
                        {}}}))))))

(deftest test-wrap-calculate-algorithm-sorting
  (testing "Wrap Calculate Algorithm Sorting"
    (let [specialist-rows [{:bhc-id 1 :wachttijd-toegangstijd-score 20 :reisafstand 0 :kwaliteit-hto 5}
                           {:bhc-id 2 :wachttijd-toegangstijd-score 0 :reisafstand 20 :kwaliteit-hto 4}
                           {:bhc-id 3 :wachttijd-toegangstijd-score 21 :reisafstand nil :kwaliteit-hto 3}
                           {:bhc-id 4 :wachttijd-toegangstijd-score nil :reisafstand 10 :kwaliteit-hto 2 :contract-score 1}]
          get-results-specialist-rows (partial get-results specialist-rows)]
      ;; nil is always last, regardless of best-on-top
      (are [expected actual] (= expected actual)
        [2 1 3 4] (get-results-specialist-rows {:sorteren "wachttijd"})
        [1 4 2 3] (get-results-specialist-rows {:sorteren "afstand"})
        [1 2 3 4] (get-results-specialist-rows {:sorteren "kwaliteit"})
        [1 2 3 4] (get-results-specialist-rows {})))))

(deftest test-wrap-calculate-algorithm-filtering-and-sorting
  (testing "Wrap Calculate Algorithm Combinations"
    (let [specialist-rows-1
          [{:bhc-id 1 :reisafstand 1 :mq-kwaliteit-score 5 :wachttijd-toegangstijd-score 1}
           {:bhc-id 2 :reisafstand 1 :mq-kwaliteit-score 4 :wachttijd-toegangstijd-score 6}
           {:bhc-id 3 :reisafstand 2 :mq-kwaliteit-score 5 :wachttijd-toegangstijd-score 1}
           {:bhc-id 4 :reisafstand 1 :mq-kwaliteit-score 5 :wachttijd-toegangstijd-score 2}]
          specialist-rows-2
          [{:bhc-id 1 :reisafstand 40 :mq-kwaliteit-score 5 :wachttijd-toegangstijd-score 1}
           {:bhc-id 2 :reisafstand 10 :mq-kwaliteit-score 4 :wachttijd-toegangstijd-score 6}
           {:bhc-id 3 :reisafstand 70 :mq-kwaliteit-score 5 :wachttijd-toegangstijd-score 1}
           {:bhc-id 4 :reisafstand 10 :mq-kwaliteit-score 5 :wachttijd-toegangstijd-score 2}]
          specialist-rows-3
          [{:bhc-id 1 :mq-kwaliteit-score 5 :wachttijd-toegangstijd-score 1 :reisafstand 10
            :reisafstand-norm 10 :mq-kwaliteit-norm 50 :wachttijd-toegangstijd-norm 100}
           {:bhc-id 2 :mq-kwaliteit-score 4 :wachttijd-toegangstijd-score 6 :reisafstand 10
            :reisafstand-norm 10 :mq-kwaliteit-norm 40 :wachttijd-toegangstijd-norm 10}
           {:bhc-id 3 :mq-kwaliteit-score 5 :wachttijd-toegangstijd-score 1 :reisafstand 5
            :reisafstand-norm 5 :mq-kwaliteit-norm 50 :wachttijd-toegangstijd-norm 100}
           {:bhc-id 4 :mq-kwaliteit-score 5 :wachttijd-toegangstijd-score 2 :reisafstand 10
            :reisafstand-norm 10 :mq-kwaliteit-norm 50 :wachttijd-toegangstijd-norm 80}]]
      (are [expected actual] (= expected actual)
        ;; usecase 1 - sorteren, wordt eigenlijk ook al getest in deftest test-wrap-calculate-algorithm-sorting
        [1 3 4 2] (get-results specialist-rows-1
                               {:sorteren "wachttijd"})
        ;; usecase 2 - persoonlijke voorkeuren
        [1 4 3 2] (get-results specialist-rows-3
                               {:afstand {:afstand-weight 10}
                                :kwaliteit {:kwaliteit-weight 90}
                                :wachttijd {:wachttijd-weight 1}})

        ;; usecase 3 - filteren, wordt eigenlijk ook al getest in deftest test-wrap-calculate-algorithm-filtering
        [1 3 4] (get-results specialist-rows-1
                             {:wachttijd {:wachttijd-filter 5}})
        ;; usecase 4 - persoonlijke voorkeuren en sorteren
        [3 1 4 2] (get-results specialist-rows-3
                               {:afstand {:afstand-weight 10}
                                :kwaliteit {:kwaliteit-weight 90}
                                :wachttijd {:wachttijd-weight 1}
                                :sorteren "afstand"})
        ;; usecase 5 - filteren en sorteren
        [1 4 2] (get-results specialist-rows-2
                             {:sorteren "wachttijd"
                              :afstand {:afstand-filter 50}})
        ;; usecase 6 - persoonlijke voorkeuren en filteren op wachttijd
        [1 4 3] (get-results specialist-rows-3
                             {:afstand {:afstand-weight 10}
                              :kwaliteit {:kwaliteit-weight 90}
                              :wachttijd {:wachttijd-filter 5
                                          :wachttijd-weight 1}})
        ;; usecase 7 - persoonlijke voorkeuren en filteren op kwaliteit
        [1 4 3] (get-results specialist-rows-3
                             {:afstand {:afstand-weight 10}
                              :kwaliteit {:kwaliteit-filter 5
                                          :kwaliteit-weight 90}
                              :wachttijd {:wachttijd-weight 1}})))))

(deftest test-wrap-valid-choice?
  (let [params {:path-params {:zorgrank-id "061aa190-1a99-4202-945f-75338c5ca8ac"}
                :body-params {:gekozen-zorgaanbieder 10}}]

    (testing "No result from database"
      (with-redefs [events-db/number-of-results-response (constantly '())]
        (is (= 404 (-> ((zorgrank-request/wrap-valid-choice? identity)
                        params)
                       :status)))))
    (testing "Number returned by database is less than number choice"
      (with-redefs [events-db/number-of-results-response (constantly '({:nr 9}))]
        (is (= 400 (-> ((zorgrank-request/wrap-valid-choice? identity)
                        params)
                       :status)))))
    (testing "Success"
      (with-redefs [events-db/number-of-results-response (constantly '({:nr 10}))]
        (is (=  [:path-params :body-params]
                (-> ((zorgrank-request/wrap-valid-choice? identity)
                     params)
                    keys
                    vec)))))))
