(ns nl.mediquest.zorgrank.middleware.logging
  (:require
   [clojure.string :as string]
   [clojure.tools.logging :as log]
   [ring.logger :refer [wrap-with-logger]]
   [ring.middleware.conditional :as middleware.conditional]))

(defn- not-request-from-internal-ip?
  "Returns truthy when a request does not come from an internal IP (e.g., a
  Kubernetes node)"
  [{:keys [server-name] :as _request}]
  (let [internal-ip-start "10."]
    (not (string/starts-with? (str server-name) internal-ip-start))))

(defn- info-level-logger
  "Logs the starting and finishing of a request on INFO level, regardless of the
  status. This differs from the default ring-logger, that:
  - logs status 500 on ERROR level (and thus causes an entry in Sentry, which we
    don't want)
  - logs type :params on DEBUG level (:params does not add much information, so
    we discard it)"
  [{{:keys [ring.logger/type]} :message :as message}]
  (when (#{:starting :finish} type)
    (log/info message)))

(defn- wrap-with-info-logger [handler]
  (let [opts {:log-exceptions? false
              :log-fn info-level-logger}]
    (wrap-with-logger handler opts)))

(defn logging-middleware [handler]
  (-> handler
      (middleware.conditional/if not-request-from-internal-ip? wrap-with-info-logger)))
