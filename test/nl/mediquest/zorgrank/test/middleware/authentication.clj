(ns nl.mediquest.zorgrank.test.middleware.authentication
  (:require
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as gen]
   [clojure.spec.test.alpha :as stest]
   [clojure.test :refer [deftest use-fixtures is are testing]]
   [nl.mediquest.zorgrank.middleware.authentication
    :refer
    [authenticate-request wrap-authentication]]
   [nl.mediquest.zorgrank.specs :as specs]))

(defn ns-fixture [tests]
  (stest/instrument)
  (tests))

(use-fixtures :once ns-fixture)

(deftest test-authenticate-request
  (let [clients (gen/generate
                 (s/gen (s/and ::specs/clients not-empty)))
        [valid-client-key organization] (-> clients  first)
        invalid-client-key (gen/generate
                            (s/gen ::specs/uuid-keyword))]
    (are [actual expected] (= (:organization actual) expected)
      (authenticate-request valid-client-key clients true) organization
      (authenticate-request invalid-client-key clients true) nil
      (authenticate-request invalid-client-key clients false) "anonymous"
      (authenticate-request nil clients false) "anonymous")))

(deftest test-wrap-authentication
  (testing "no clients, no client key in header and authentication not
  active"
    (is (= (:organization ((wrap-authentication identity {} false)
                           {:headers {}}))
           "anonymous")))
  (testing "no clients, no client key in header and authentication
  active"
    (is (= (:status ((wrap-authentication identity {} true)
                     {:headers {}}))
           403))))
