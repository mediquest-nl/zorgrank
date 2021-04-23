(ns nl.mediquest.zorgrank-load-test.scenarios
  (:require
   [nl.mediquest.zorgrank-load-test.steps :as steps]))

(def zorgrank-test
  {:name "Zorgrank request"
   :steps [steps/zorgrank-request
           steps/zorgrank-choice]})
