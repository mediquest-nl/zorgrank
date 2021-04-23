(ns nl.mediquest.zorgrank.middleware.sentry
  (:require
   [clojure.pprint :refer [pprint]]
   [clojure.tools.logging :as log]
   [nl.mediquest.zorgrank.config :refer [env]]
   [ring.util.http-response :as http-response])
  (:import
   (io.sentry Sentry)))

(defn- sentry-capture [e]
  (when-let [data (ex-data e)]
    (.addExtra (Sentry/getContext) "ex-data"
               (with-out-str (pprint data))))
  (Sentry/capture e)
  (Sentry/clearContext))

(def sentry-error-handler
  "If `sentry-dsn` is not nil this handler tries to add some user
  context from the session, sends the exception to Sentry and returns
  a generic HTTP 500 Internal Server Error; otherwise it will log the
  exception in a normal way."
  (fn [^Exception e _]
    (log/info e (.getMessage e))
    (if (env :sentry-dsn)
      (sentry-capture e)
      (log/error e (.getMessage e)))
    (http-response/internal-server-error
     {:error "Oeps, er ging iets mis, we zijn op de hoogte gesteld."})))
