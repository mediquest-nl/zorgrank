(ns nl.mediquest.zorgrank.test.handler
  (:require
   [clojure.spec.test.alpha :as stest]
   [clojure.test :refer [deftest testing is use-fixtures]]
   [mount.core :as mount]
   [muuntaja.core :as m]
   [nl.mediquest.zorgrank.config :as config]
   [nl.mediquest.zorgrank.handler :refer [app] :as handler]
   [nl.mediquest.zorgrank.middleware.formats :as formats]
   [ring.mock.request :refer [request]]))

(defn parse-json [body]
  (m/decode formats/instance "application/json" body))

(use-fixtures
  :once
  (fn [f]
    (stest/instrument)
    (mount/start #'config/env
                 #'config/clients
                 #'handler/app)
    (f)))

(deftest test-app
  (testing "main route"
    (let [response (app (request :get "/api/version"))]
      (is (= 200 (:status response)))))
  (testing "redirect `/` to swagger"
    (let [response (app (request :get "/"))]
      (is (= 301 (:status response))))))

;; testing "services"

(comment (let [response (app
                         (-> (request :post "/api/v2/zorgrank-request")
                             (json-body {:verwijzing
                                         {:specialisme
                                          {:specialisme_code "0301",
                                           :specialisme_codestelsel "COD016-VEKT"}}
                                         :patient
                                         {:postcode "1013AA"}})))]
           (-> response m/decode-response-body))

         (let [response (app
                         (-> (request :post "/api/v2/zorgrank-choice")
                             (json-body {:zorgrank_id "00000000-0000-0000-0000-000000000000",
                                         :gebruiker 1,
                                         :gekozen_zorgaanbieder 1})))]
           (-> response m/decode-response-body)))
