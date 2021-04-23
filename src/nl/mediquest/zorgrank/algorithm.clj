(ns nl.mediquest.zorgrank.algorithm
  (:require
   [clojure.spec.alpha :as s]
   [nl.mediquest.zorgrank.specs :as specs]
   [nl.mediquest.zorgrank.util :refer [round]]))

(s/fdef score
  :args (s/cat :user-weights ::specs/user-weights
               :normalized-row ::specs/normalized-row)
  :ret ::specs/score)
(defn score [user-weights normalized-row]
  (let [result (+ (* (:kwaliteit-weight user-weights)
                     (or (:mq-kwaliteit-norm normalized-row) 0))
                  (* (:wachttijd-weight user-weights)
                     (or (:wachttijd-toegangstijd-norm normalized-row) 0))
                  (* (:afstand-weight user-weights)
                     (or (:reisafstand-norm normalized-row) 0)))]
    (round 1 result)))
