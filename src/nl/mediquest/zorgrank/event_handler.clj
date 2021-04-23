(ns nl.mediquest.zorgrank.event-handler
  (:require
   [clojure.core.async :refer [>!! <! chan] :as async]
   [clojure.tools.logging :as log]
   [mount.core :refer [defstate]]
   [nl.mediquest.zorgrank.config :refer [env]]
   [nl.mediquest.zorgrank.db.events :as events-db])
  (:import
   (java.time LocalDateTime)))

(defmulti build-event
  "Build event based on type of request,
  Primarily used by middleware function `wrap-send-event`. When used by
  `wrap-send-event`, `type` is a concat of uri and type parameter in
  middleware."
  :type)

(defmethod build-event "/api/v3/zorgrank-request/request"
  [{:keys [_uri body-params uuid patient-id type organization authenticated]}]
  {:body (assoc body-params
                :patient-id patient-id
                :organization organization
                :authenticated authenticated)
   :inserted_at (LocalDateTime/now)
   :uuid uuid
   :type type})

(defmethod build-event "/api/v3/zorgrank-request/response"
  [{:keys [_uri _response data uuid patient-id type organization authenticated]}]
  {:body {:data data
          :patient-id patient-id
          :organization organization
          :authenticated authenticated}
   :inserted_at (LocalDateTime/now)
   :uuid uuid
   :type type})

(defmethod build-event "/api/v3/zorgrank-choice/request"
  [{:keys [_uri body-params path-params type organization authenticated]}]
  (let [uuid (:zorgrank-id path-params)]
    {:body (assoc body-params
                  :organization organization
                  :authenticated authenticated)
     :inserted_at (LocalDateTime/now)
     :uuid uuid
     :type type}))

(defmethod build-event "/api/v3/upload/wachttijden/request"
  [{:keys [_uri body-params type organization authenticated uuid] :as _request}]
  {:body (assoc body-params
                :organization organization
                :authenticated authenticated)
   :inserted_at (LocalDateTime/now)
   :uuid uuid
   :type type})

(defmethod build-event "/api/v3/upload/zorgaanbieders/request"
  [{:keys [_uri body-params type organization authenticated uuid] :as _request}]
  {:body (assoc body-params
                :organization organization
                :authenticated authenticated)
   :inserted_at (LocalDateTime/now)
   :uuid uuid
   :type type})

(defmethod build-event "/api/v3/upload/patientervaringen/request"
  [{:keys [body-params type organization authenticated uuid] :as _request}]
  {:body (assoc body-params
                :organization organization
                :authenticated authenticated)
   :inserted_at (LocalDateTime/now)
   :uuid uuid
   :type type})

(defmulti writer
  "Write event to `EVENT_WRITER`.
  If write to log if no `EVENT_WRITER` is specified."
  (fn [writer _event] writer))

(defmethod writer
  :default
  [_ event]
  (log/info event))

(defmethod writer
  "event-db"
  [_ event]
  (try
    (events-db/add! event)
    (catch Exception e
      (log/error e ::writer-event-db {:message (.getMessage e) :event event})
      (throw e))))

(defn produce
  "Helper for putting request on event channel"
  [ch request]
  (>!! ch request))

(defn make-event-engine
  "Process requests to events.
  Uses 2 multimethods:
  - `build-event` for dispatching on type of request
  - `writer` for dispatching on type of writer
  "
  []
  (let [ch (chan (async/dropping-buffer 10000)
                 (map build-event))]
    (async/go-loop []
      (when-let [event (<! ch)]
        (writer (env :event-writer) event)
        (recur)))
    {:stop (fn []
             (async/close! ch)
             (log/info "event engine is stopped"))
     :process (partial produce ch)}))

(defstate event-engine :start (make-event-engine)
  :stop ((:stop event-engine)))
