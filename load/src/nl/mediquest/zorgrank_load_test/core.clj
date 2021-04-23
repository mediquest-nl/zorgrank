(ns nl.mediquest.zorgrank-load-test.core
  (:require
   [clj-gatling.core :as clj-gatling]
   [clojure.tools.logging :as log]
   [nl.mediquest.zorgrank-load-test.scenarios :as scenarios])
  (:gen-class))

(defn -main
  [& args]
  (if-let [host (first args)]
    (clj-gatling/run
      {:name "zorgrank_api_load_test"
       :scenarios [scenarios/zorgrank-test]}
      {:context {:host host}
       :requests 3000
       :concurrency 20})
    (do
      (log/warn "Host is required. E.g.,")
      (log/warn (str "clj -m nl.mediquest.zorgrank-load-test.core "
                     "https://zorgrank-staging.mediquest.dev")))))
