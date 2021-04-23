(ns nl.mediquest.zorgrank-load-test.steps
  (:require
   [cheshire.core :as json]
   [clojure.core.async :refer [go]]
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [environ.core :refer [env]]
   [org.httpkit.client :as http]))

(def api-path "/api/v3/")
(def client-key (env :client-key))

(defn- file->edn-vector [resource]
  (->> resource
       io/resource
       slurp
       (format "[%s]")
       edn/read-string))

(def valid-pc4s
  (file->edn-vector "valid_pc4s.txt"))

(def valid-specialties
  (file->edn-vector "specialties.txt"))

(def problem-namecodes
  (file->edn-vector "problems.txt"))

(def uzovi->pakketcodes
  (first (file->edn-vector "contracts.edn")))

(defn gen-zorgrank-request-json []
  (let [specialisme-code (rand-nth valid-specialties)
        problem-name-code (rand-nth problem-namecodes)
        number-results (rand-int 100)
        gen-weight #(rand-int 100)
        postal-code (format "%04dAA" (rand-nth valid-pc4s))
        uzovi (rand-nth (keys uzovi->pakketcodes))
        pakketcode (rand-nth (get uzovi->pakketcodes uzovi))]
    (json/encode
     {:verwijzing {:specialisme {:specialisme_codestelsel "COD016-VEKT"
                                 :specialisme_code specialisme-code}}
      :voorkeuren {:wachttijd {:weight (gen-weight)}
                   :afstand {:weight (gen-weight)}
                   :kwaliteit {:weight (gen-weight)}}
      :probleem {:probleem-naamcode problem-name-code
                 :probleem-codestelsel "icpc-1 NL"}
      :patient {:postcode postal-code
                :zorgverzekering {:uzovi uzovi
                                  :pakketcode pakketcode}}
      :number-results number-results})))

(def zorgrank-request
  {:name "zorgrank request"
   :sleep-before (fn [ctx] (rand-int 1000)) ;; Spread requests somewhat
   :request (fn [{:keys [host] :as context}]
              (go
                (let [{:keys [status body] :as _res}
                      @(http/post (str host api-path "zorgrank-request")
                                  {:insecure? true
                                   :headers {"Content-Type" "application/json; charset=utf-8"
                                             "client-key" client-key}
                                   :body (gen-zorgrank-request-json)})
                      zorgrank-id (:zorgrank_id (json/decode body true))
                      success? (= status 200)
                      new-context (assoc context :zorgrank-id zorgrank-id)]
                  [success? new-context])))})

(defn gen-zorgrank-choice-json []
  (let [patient 1
        tipp 2
        huisarts 3
        user-set #{patient tipp huisarts}]
    (json/encode
     {:gebruiker (rand-nth (seq user-set)),
      :gekozen_zorgaanbieder 1})))

(def zorgrank-choice
  {:name "zorgrank choice"
   :sleep-before (constantly 2500) ;; Give the Zorgrank API some time to store
   ;; zorgrank-id before it is retrieved
   :request (fn [{:keys [host zorgrank-id] :as _context}]
              (go
                (let [{:keys [status] :as _res}
                      @(http/put (str host api-path "zorgrank-choice/" zorgrank-id)
                                 {:insecure? true
                                  :headers {"Content-Type" "application/json; charset=utf-8"
                                            "client-key" client-key}
                                  :body (gen-zorgrank-choice-json)})]
                  (= status 200))))})
