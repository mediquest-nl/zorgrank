(ns nl.mediquest.zorgrank.middleware.zorgrank-request
  (:require
   [clojure.spec.alpha :as s]
   [nl.mediquest.zorgrank.algorithm :as algorithm]
   [nl.mediquest.zorgrank.config :as config :refer [*print-sql*]]
   [nl.mediquest.zorgrank.db.events :as events-db]
   [nl.mediquest.zorgrank.db.zorgrank-datamart :as zorgrank-datamart-db]
   [nl.mediquest.zorgrank.db.zorgrank-datamart-aandoeningen :as zorgrank-datamart-aandoeningen-db]
   [nl.mediquest.zorgrank.event-handler :refer [event-engine]]
   [nl.mediquest.zorgrank.specs :as specs]
   [nl.mediquest.zorgrank.specs.api.zorgrank-request.request :as specs.zorgrank-request.request]
   [nl.mediquest.zorgrank.specs.api.zorgrank-request.response :as zorgrank-request.response]
   [nl.mediquest.zorgrank.util :as util])
  (:import
   (java.util UUID)))

(s/fdef body-params->specialisme-code
  :args (s/cat :request ::specs.zorgrank-request.request/zorgrank-request)
  :ret ::specs/specialisme-code)
(def ^:private body-params->specialisme-code
  (comp :specialisme-code
        :specialisme
        :verwijzing))

(s/fdef patient->pc4
  :args (s/cat :patient ::specs.zorgrank-request.request/patient)
  :ret ::specs/pc-4)
(def ^:private patient->pc4
  (comp #(Integer. %)
        #(subs % 0 4)
        :postcode))

(s/fdef has-postcode?
  :args (s/cat :patient ::specs.zorgrank-request.request/patient)
  :ret boolean?)
(def ^:private has-postcode?
  (comp some?
        :postcode))

(s/fdef has-pakketcode-and-uzovi?
  :args (s/cat :patient (s/nilable ::specs.zorgrank-request.request/zorgverzekering))
  :ret boolean?)
(def ^:private has-pakketcode-and-uzovi?
  (comp (partial s/valid?
                 ::specs.zorgrank-request.request/pakketcode-and-uzovi)))

(s/fdef body-params->uzovi
  :args (s/cat :request ::specs.zorgrank-request.request/zorgrank-request)
  :ret (s/nilable ::specs.zorgrank-request.request/uzovi))
(defn- body-params->uzovi [{{:keys [zorgverzekering]} :patient :as _request}]
  (when (has-pakketcode-and-uzovi? zorgverzekering)
    (:uzovi zorgverzekering)))

(s/fdef body-params->pakketcode
  :args (s/cat :request ::specs.zorgrank-request.request/zorgrank-request)
  :ret (s/nilable ::specs.zorgrank-request.request/pakketcode))
(defn- body-params->pakketcode [{{:keys [zorgverzekering]} :patient :as _request}]
  (when (has-pakketcode-and-uzovi? zorgverzekering)
    (-> zorgverzekering
        :pakketcode)))

(s/fdef body-params->pc4
  :args (s/cat :request ::specs.zorgrank-request.request/zorgrank-request)
  :ret (s/nilable ::specs/pc-4))
(defn- body-params->pc4 [{:keys [patient] :as _request}]
  (when (and patient
             (has-postcode? patient))
    (patient->pc4 patient)))

(s/fdef body-params->icpc
  :args (s/cat :request ::specs.zorgrank-request.request/zorgrank-request)
  :ret (s/nilable ::specs/probleem-naamcode))
(def ^:private body-params->icpc
  (comp :probleem-naamcode
        :probleem
        :verwijzing))

(defn- request->zorgrank-query-args [{:keys [body-params] :as request}]
  {:specialty (body-params->specialisme-code body-params)
   :organization (:organization request)
   :icpc (body-params->icpc body-params)
   :pc4 (body-params->pc4 body-params)
   :uzovi (body-params->uzovi body-params)
   :pakketcode (body-params->pakketcode body-params)
   :datamart "zorgrank_datamart"})

(defn- query-zorgrank-data [zorgrank-query-args]
  (if (:icpc zorgrank-query-args)
    (do (when *print-sql*
          (println (util/sql-str (zorgrank-datamart-aandoeningen-db/get-data-sqlvec zorgrank-query-args))))
        (zorgrank-datamart-aandoeningen-db/get-data zorgrank-query-args))
    (do (when *print-sql*
          (println (util/sql-str (zorgrank-datamart-db/get-data-sqlvec zorgrank-query-args))))
        (zorgrank-datamart-db/get-data zorgrank-query-args))))

(defn wrap-send-event
  "Middleware to send request or response as event to an event store.

  Parameters:
  `handler`: the handler to wrap
  `type`: usually \"request\" or \"response\""
  ([handler]
   (wrap-send-event handler nil))
  ([handler type]
   (fn [{:keys [uri path-params] :as request}]
     (let [uri (if path-params
                 (util/remove-path-params uri path-params)
                 uri)
           type (apply str uri (when type [\/ type]))]
       ((event-engine :process) (assoc request :type type))
       (handler request)))))

(defn- get-zorgrank-data
  "When we query by icpc and no results are returned, we query by specialty as a
  fallback. Specialty is required, whereas icpc is not."
  [zorgrank-query-args]
  (let [result (query-zorgrank-data zorgrank-query-args)
        result (remove (comp nil? :specialisme) result)]
    (if (and (empty? result)
             (:icpc zorgrank-query-args))
      (-> zorgrank-query-args
          (assoc :icpc nil)
          query-zorgrank-data)
      result)))

(defn- get-type
  [type-column row]
  (type-column row))

(defn- get-code
  "Get 'aandoening' or 'specialisme' code from database row based on
  value of key `type-column`.

  E.g. `{:zorgcontractering-type \"specialisme\"}` returns value of
  `{:specialisme \"T90.02\"}`."
  [type-column row]
  (-> row
      type-column
      keyword
      row))

(defn- get-code-extra
  "Get 'aandoening' or 'specialisme' description from database row based
  on value of key `type-column`.

  E.g. `{:zorgcontractering-type \"specialisme\"}` returns value of
  `{:specialisme-beschrijving \"Diabetes Type 2\"}`."
  [type-code type-column row]
  (->> row
       type-column
       (format "%2$s-%1$s" type-code)
       keyword
       row))

(def get-code-naam
  (partial get-code-extra "code-naam"))

(def get-code-naam-synoniem
  (partial get-code-extra "code-naam-synoniem"))

(defn- hto-data
  [{:keys [hto-score hto-aantal] :as row}]
  (when hto-score
    {:score hto-score
     :aantal hto-aantal
     :code-naam (get-code-naam :hto-type row)
     :code-naam-synoniem (get-code-naam-synoniem :hto-type row)
     :code (get-code :hto-type row)
     :type (get-type :hto-type row)}))

(defn- assoc-hto-data
  [row]
  (assoc row
         :hto
         (hto-data row)))

(defn- wachttijd-toegangstijd
  [{:keys [wachttijd-toegangstijd-score] :as row}]
  (when wachttijd-toegangstijd-score
    {:score wachttijd-toegangstijd-score
     :code-naam (get-code-naam :wachttijd-toegangstijd-type row)
     :code-naam-synoniem (get-code-naam-synoniem :wachttijd-toegangstijd-type row)
     :code (get-code :wachttijd-toegangstijd-type row)
     :type (get-type :wachttijd-toegangstijd-type row)}))

(defn- assoc-wachttijd-toegangstijd
  [row]
  (assoc row
         :wachttijd-toegangstijd
         (wachttijd-toegangstijd row)))

(defn- wachttijd-behandeltijd
  [{:keys [wachttijd-behandeltijd-score] :as row}]
  (when wachttijd-behandeltijd-score
    {:score wachttijd-behandeltijd-score
     :code-naam (get-code-naam :wachttijd-behandeltijd-type row)
     :code-naam-synoniem (get-code-naam-synoniem :wachttijd-behandeltijd-type row)
     :code (get-code :wachttijd-behandeltijd-type row)
     :type (get-type :wachttijd-behandeltijd-type row)}))

(defn- assoc-wachttijd-behandeltijd
  [row]
  (assoc row
         :wachttijd-behandeltijd
         (wachttijd-behandeltijd row)))

(defn- kwic-totaalscore-data
  [{:keys [kwic-totaalscore-score] :as row}]
  (when kwic-totaalscore-score
    {:score kwic-totaalscore-score
     :code-naam (get-code-naam :kwic-totaalscore-type row)
     :code-naam-synoniem (get-code-naam-synoniem :kwic-totaalscore-type row)
     :code (get-code :kwic-totaalscore-type row)
     :type (get-type :kwic-totaalscore-type row)}))

(defn- assoc-kwic-totaalscore-data
  [row]
  (assoc row
         :kwic-totaalscore
         (kwic-totaalscore-data row)))

(defn- kwic-sterscore-data
  [{:keys [kwic-sterscore-score] :as row}]
  (when kwic-sterscore-score
    {:score kwic-sterscore-score
     :code-naam (get-code-naam :kwic-sterscore-type row)
     :code-naam-synoniem (get-code-naam-synoniem :kwic-sterscore-type row)
     :code (get-code :kwic-sterscore-type row)
     :type (get-type :kwic-sterscore-type row)}))

(defn- assoc-kwic-sterscore-data
  [row]
  (assoc row
         :kwic-sterscore
         (kwic-sterscore-data row)))

(defn- patientervaring-data
  [{:keys [patientervaring-score patientervaring-aantal] :as row}]
  (when patientervaring-score
    {:score patientervaring-score
     :code-naam (get-code-naam :patientervaring-type row)
     :code-naam-synoniem (get-code-naam-synoniem :patientervaring-type row)
     :code (get-code :patientervaring-type row)
     :type (get-type :patientervaring-type row)
     :aantal patientervaring-aantal}))

(defn- assoc-patientervaring-data
  [row]
  (assoc row
         :patientervaring
         (patientervaring-data row)))

(defn- get-zorgcontracteringscore-description
  [type score]
  (get-in config/zorgcontracteringscore-descriptions [score type]))

(defn- zorgcontracterings-data
  [{:keys [contract-score] :as row}]
  (when contract-score
    {:score contract-score
     :score-beschrijving (get-zorgcontracteringscore-description :long contract-score)
     :score-titel (get-zorgcontracteringscore-description :short contract-score)
     :code-naam (get-code-naam :contract-type row)
     :code-naam-synoniem (get-code-naam-synoniem :contract-type row)
     :code (get-code :contract-type row)
     :type (get-type :contract-type row)}))

(defn- assoc-zorgcontracterings-data
  [row]
  (assoc row
         :zorgcontractering
         (zorgcontracterings-data row)))

(def ^:private nested-scores
  (comp assoc-zorgcontracterings-data
        assoc-kwic-totaalscore-data
        assoc-kwic-sterscore-data
        assoc-patientervaring-data
        assoc-hto-data
        assoc-wachttijd-toegangstijd
        assoc-wachttijd-behandeltijd))

(defn wrap-get-data-from-zorgrank-datamart
  [handler]
  (fn [request]
    (->> request
         request->zorgrank-query-args
         get-zorgrank-data
         (assoc request :specialist-rows)
         handler)))

(defn wrap-add-uuid
  "UUID will be given back in the response of `zorgrank-request` so
  client can use this identifier when posting `zorgrank-choice`."
  [handler]
  (fn [request]
    (let [uuid (UUID/randomUUID)]
      (handler (assoc request :uuid uuid)))))

(s/fdef user-weights
  :args (s/cat :voorkeuren ::specs.zorgrank-request.request/voorkeuren)
  :ret ::specs/weight)
(defn- get-user-weights [{:keys [wachttijd afstand kwaliteit]}]
  {:afstand-weight (:afstand-weight afstand 1)
   :kwaliteit-weight (:kwaliteit-weight kwaliteit 1)
   :wachttijd-weight (:wachttijd-weight wachttijd 1)})

(defn- wachttijd-predicate [wachttijd-filter]
  (fn [row]
    (if wachttijd-filter
      (and (:wachttijd-toegangstijd-score row) (<= (:wachttijd-toegangstijd-score row) wachttijd-filter))
      true)))

(defn- afstand-predicate [reisafstand-filter]
  (fn [row]
    (if reisafstand-filter
      (or (nil? (:reisafstand row))
          (and (:reisafstand row) (<= (:reisafstand row) reisafstand-filter)))
      true)))

(defn- kwaliteit-predicate [kwaliteit-filter]
  (fn [row]
    (if kwaliteit-filter
      (>= (or (:mq-kwaliteit-score row) 0) kwaliteit-filter)
      true)))

(defn- contractering-predicate [contractering-filter]
  (fn [{:keys [contract-score] :as _row}]
    (if (seq contractering-filter)
      (some #(= contract-score %) contractering-filter)
      true)))

(defn- get-voorkeuren-predicates
  [{:keys [wachttijd afstand kwaliteit contractering] :as _voorkeuren}]
  [(wachttijd-predicate (:wachttijd-filter wachttijd))
   (afstand-predicate (:afstand-filter afstand))
   (kwaliteit-predicate (:kwaliteit-filter kwaliteit))
   (contractering-predicate (:contractering-filter contractering))])

(defn- voorkeuren-predicate [predicates specialist-row]
  (every? #(% specialist-row) predicates))

(defn wrap-add-scores [handler]
  (fn [request]
    (let [data (:data request)]
      (->> data
           (map nested-scores)
           (map zorgrank-request.response/select-response-keys)
           (assoc request :data)
           handler))))

(defn wrap-valid-choice? [handler]
  (fn [{:keys [path-params body-params] :as request}]
    (let [zorgrank-id (:zorgrank-id path-params)
          number-choice (:gekozen-zorgaanbieder body-params)
          number-of-results-response (-> {:type "/api/v2/zorgrank-request/response"
                                          :zorgrank-id zorgrank-id}
                                         (events-db/number-of-results-response)
                                         first
                                         :nr)]
      (cond
        (nil? number-of-results-response)
        {:status 404
         :headers {"Content-Type" "text/plain"}
         :body "ZorgRank-id not known"}

        (> number-choice number-of-results-response)
        {:status 400
         :headers {"Content-Type" "text/plain"}
         :body "Number 'gekozen-zorgaanbieder' is higher than number of health care providers in response"}

        :else
        (handler request)))))

(def ^:private ref-sorting-keys
  {"wachttijd" {:id :wachttijd-toegangstijd-score :order util/lower-is-better}
   "afstand" {:id :reisafstand :order util/lower-is-better}
   "kwaliteit" {:id :mq-kwaliteit-score :order util/higher-is-better}})

(defn- x-sort-voorkeuren
  [sorting-key]
  (if-let [{:keys [id order]} (get ref-sorting-keys sorting-key)]
    (util/xf-sort-by id order)
    identity))

(def x-sort-score
  (util/xf-sort-by :score util/higher-is-better))

(defn- add-score [user-weights specialist-row]
  (->> specialist-row
       (algorithm/score user-weights)
       (assoc specialist-row :score)))

(defn- x-add-score [user-weights]
  (map (partial add-score user-weights)))

(defn- x-filter-by-voorkeuren
  [voorkeuren]
  (let [voorkeuren-predicates (get-voorkeuren-predicates voorkeuren)]
    (filter (partial voorkeuren-predicate voorkeuren-predicates))))

(def x-add-rank
  (map-indexed (fn [i row] (assoc row :rank (inc i)))))

(defn x-calculation
  [user-weights voorkeuren sorteren number-results]
  (comp (x-add-score user-weights)
        (x-filter-by-voorkeuren voorkeuren)
        x-sort-score
        (x-sort-voorkeuren sorteren)
        x-add-rank
        (take (or number-results 5))))

(defn wrap-calculate-algorithm [handler]
  (fn [request]
    (let [specialist-rows (:specialist-rows request)
          {:keys [sorteren number-results] :as voorkeuren} (get-in request [:body-params :voorkeuren])
          user-weights (get-user-weights voorkeuren)]
      (->> specialist-rows
           (into []
                 (x-calculation user-weights
                                voorkeuren
                                sorteren
                                number-results))
           (assoc request :data)
           handler))))
