(ns nl.mediquest.zorgrank-client.events
  (:require
   [nl.mediquest.zorgrank-client.component.dropdown.core :as component.dropdown]
   [nl.mediquest.zorgrank-client.component.notifications.core :as component.notifications]
   [adzerk.env :as env]
   [ajax.core :as ajax]
   [nl.mediquest.zorgrank-client.db :as db]
   [re-frame.core :as re-frame]))

(defn valid-postcode? [postcode]
  (when postcode
    (re-matches #"[0-9]{4} ?[a-zA-Z]{0,2}" postcode)))

(defn initial-zorgrank-request-payload [db]
  {:verwijzing {:specialisme {:specialisme_codestelsel "COD016-VEKT"
                              :specialisme_code (:specialisme-code db)}
                :probleem {:probleem_codestelsel "icpc-1 NL"}}
   :voorkeuren {:wachttijd {:wachttijd-weight (:slider-wachttijd db 1)}
                :afstand {:afstand-weight (:slider-afstand db 1)}
                :kwaliteit {:kwaliteit-weight (:slider-kwaliteit db 1)}}})

(defn add-postcode [payload db]
  (if-let [postcode (:postcode db)]
    (assoc-in payload [:patient :postcode] postcode)
    payload))

(defn build-zorgrank-request-payload [db]
  (-> db
      initial-zorgrank-request-payload
      (add-postcode db)
      (cond->
          (component.dropdown/selected "ICPC")
          (assoc-in [:verwijzing :probleem :probleem_naamcode]
                    (component.dropdown/selected "ICPC")))
      (cond->
          (component.dropdown/selected "Uzovi")
          (assoc-in [:patient :zorgverzekering :uzovi]
                    (component.dropdown/selected "Uzovi")))
      (cond->
          (component.dropdown/selected "Pakketcode")
          (assoc-in [:patient :zorgverzekering :pakketcode]
                    (component.dropdown/selected "Pakketcode")))
      (cond->
          (component.dropdown/selected "Zorgcontractering")
          (assoc-in [:voorkeuren :contractering :contractering_filter]
                    (component.dropdown/selected "Zorgcontractering")))
      (cond->
          (component.dropdown/selected "Sortering")
          (assoc-in [:voorkeuren :sorteren]
                    (component.dropdown/selected "Sortering")))
      (cond->
          (:filter-wachttijd db)
          (assoc-in [:voorkeuren :wachttijd :wachttijd_filter]
                    (:filter-wachttijd db)))
      (cond->
          (:filter-afstand db)
          (assoc-in [:voorkeuren :afstand :afstand_filter]
                    (:filter-afstand db)))
      (cond->
          (:filter-kwaliteit db)
          (assoc-in [:voorkeuren :kwaliteit :kwaliteit_filter]
                    (:filter-kwaliteit db)))))

(env/def ZORGRANK_URI
  "http://localhost:3030/api/v3/zorgrank-request")

(defn zorgrank-request [db]
  {:method :post
   :uri ZORGRANK_URI
   :params (build-zorgrank-request-payload db)
   :timeout 8000
   :response-format (ajax/json-response-format {:keywords? true})
   :format (ajax/json-request-format)
   :on-success [::success-http-result]
   :on-failure [::failure-http-result]})

(defn add-headers [db request]
  (if-let [client-key (:auth/client-key db)]
    (assoc-in request [:headers :client-key] client-key)
    request))

(defn wrap-request-in-xhrio [request]
  {:http-xhrio request})

(defn send-request? [db]
  (and (:specialisme-code db)
       (not (:mouse-down db))
       (some identity ((juxt nil? valid-postcode?) (:postcode db)))))

(defn add-zorgrank-request [{:keys [db] :as cofx}]
  (if (send-request? db)
    (->> (zorgrank-request db)
         (add-headers db)
         (wrap-request-in-xhrio)
         (merge cofx))
    cofx))

(re-frame/reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))

(re-frame/reg-event-db
 ::success-http-result
 (fn [db [_ result]]
   (assoc db :zorgrank-response (:data result))))

(re-frame/reg-event-db
 ::failure-http-result
 (fn [db [_ result]]
   (component.notifications/add-notification
    {:notification/type :notification/http-error
     :notification/level :level/danger
     :notification/content "Error contacting server:"
     :notification/data result})
   db))

(re-frame/reg-event-db
 :specialist/choose
 (fn [db [_ specialist]]
   (assoc db :specialist/selected specialist)))

(re-frame/reg-event-db
 :specialist/confirm
 (fn [db [_ specialist]]
   (assoc db :specialist/confirmed specialist)))

(re-frame/reg-event-fx
 :set-mouse-down
 (fn [{:keys [db]} [_ mouse-down]]
   (let [updated-db (assoc db :mouse-down mouse-down)]
     (add-zorgrank-request {:db updated-db}))))

(re-frame/reg-event-fx
 :slider-event
 (fn [{:keys [db]} [_ slider-id value]]
   (when-not (js/isNaN value)
     (let [updated-db (assoc db slider-id (js/parseInt value))]
       (add-zorgrank-request {:db updated-db})))))

(defn set-filter-update-db [db filter-id value]
  (cond (empty? value) (assoc db filter-id nil)
        (js/isNaN value) db
        :else (assoc db filter-id (js/parseInt value))))

(re-frame/reg-event-fx
 :set-filter
 (fn [{:keys [db]} [_ filter-id value]]
   (let [updated-db (set-filter-update-db db filter-id value)]
       (add-zorgrank-request {:db updated-db}))))

(re-frame/reg-event-fx
 :set-specialisme-code
 (fn [{:keys [db]} [_ specialisme-code]]
   (let [updated-db (assoc db :specialisme-code specialisme-code)]
     (add-zorgrank-request {:db updated-db}))))

(re-frame/reg-event-fx
 :set-icpc-code
 (fn [{:keys [db]} [_ icpc-code]]
   (let [updated-db (assoc db :icpc-code icpc-code)]
     (add-zorgrank-request {:db updated-db}))))

(re-frame/reg-event-db
 :auth/set-client-key
 (fn [db [_ client-key]]
   (assoc db :auth/client-key client-key)))

(re-frame/reg-event-fx
 :set-postcode
 (fn [{:keys [db]} [_ postcode]]
   (let [updated-db (assoc db :postcode (not-empty postcode))]
     (add-zorgrank-request {:db updated-db}))))

(re-frame/reg-event-fx
 :api/zorgrank-request
 (fn [{:keys [db]} [_]]
   (when (send-request? db)
     (->> (zorgrank-request db)
          (add-headers db)
          (wrap-request-in-xhrio)))))

(re-frame/reg-event-fx
 :standaard-waarden
 (fn [{:keys [db]}]
   {:dispatch [:api/zorgrank-request]
    :db (-> db
            (assoc :postcode "3581KP"
                   :slider-afstand 30
                   :slider-kwaliteit 1
                   :slider-wachttijd 1
                   :specialisme-code "0320")
            (assoc-in [:dropdowns "Uzovi" :selected] #{{:value "3311" :content "3311"}})
            (assoc-in [:dropdowns "Pakketcode" :selected] #{{:value "106" :content "106"}}))}))
