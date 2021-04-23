(ns nl.mediquest.zorgrank.test.util
  (:require
   [clojure.spec.test.alpha :as stest]
   [clojure.test :refer [deftest use-fixtures is]]
   [nl.mediquest.zorgrank.util :refer [remove-path-params]]))

(defn ns-fixture [tests]
  (stest/instrument)
  (tests))

(use-fixtures :once ns-fixture)

(deftest test-remove-path-params
  (let [uri "uri/with/foo/and/bar"
        path-params {:param1 "foo"
                     :param2 "bar"}]
    (is (= "uri/with/and" (remove-path-params uri path-params)))))
