(ns nl.mediquest.zorgrank.sentry
  (:require
   [clojure.tools.logging :as log]
   [nl.mediquest.zorgrank.config :refer [env]]
   [mount.core :as mount :refer [defstate]])
  (:import
   (io.sentry Sentry)))

(defn app-version
  "Version of the app.
  APP_VERSION env variable is added in the container during the deployment step."
  []
  (:app-version env "UNKNOWN"))

(def package-name
  "nl.mediquest.zorgrank")

(defn start [dsn deployment-env]
  (log/info "initializing Sentry SDK")
  (Sentry/init (format "%s?release=%s&environment=%s&stacktrace.app.packages=%s"
                       dsn
                       (app-version)
                       deployment-env
                       package-name)))

(defstate sentry
  :start
  (if-let [sentry-dsn (env :sentry-dsn)]
    (start sentry-dsn (:deployment-env env "development"))
    (log/info "Sentry is not initialized because SENTRY_DSN is not set")))
