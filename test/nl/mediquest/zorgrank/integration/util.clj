(ns nl.mediquest.zorgrank.integration.util
  (:require
   [cheshire.core :refer [generate-string parse-string]]
   [clojure.spec.test.alpha :as stest]
   [luminus-migrations.core :as migrations]
   [mount.core :as mount]
   [nl.mediquest.zorgrank.config :refer [env]]
   [nl.mediquest.zorgrank.core :refer [stop-app]]
   [org.httpkit.client :as http]))

(def host
  "http://127.0.0.1:3030")

(def default-body
  {:verwijzing {:specialisme {:specialisme_codestelsel "COD016-VEKT"
                              :specialisme_code "0320"}}})

(defn each-fixture
  [test]
  (mount/start-without #'nl.mediquest.zorgrank.core/repl-server)
  (migrations/migrate ["migrate"] env)
  (stest/instrument)
  (test)
  (stop-app))

(defn send-http-post [url body]
  @(http/post (str host "/" url)
              {:body (generate-string (merge default-body body))
               :headers {"Content-Type" "application/json"}}))

(defn get-body-from-result [result]
  (-> result
      :body
      (parse-string true)))

(defn post
  ([url] (post {} url))
  ([body url]
   (let [result (send-http-post url body)]
     {:body (get-body-from-result result)
      :status (:status result)})))
