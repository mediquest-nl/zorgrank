(ns nl.mediquest.zorgrank.core
  (:require
   [clojure.java.io :as io]
   [clojure.tools.cli :refer [parse-opts]]
   [clojure.tools.logging :as log]
   [luminus-migrations.core :as migrations]
   [luminus.http-server :as http]
   [mount.core :as mount :refer [defstate]]
   [nl.mediquest.zorgrank.config :refer [env]]
   [nl.mediquest.zorgrank.handler :as handler]
   [nl.mediquest.zorgrank.nrepl :as nrepl]
   [nl.mediquest.zorgrank.sentry :refer [sentry]])
  (:gen-class))

(def cli-options
  [["-p" "--port PORT" "Port number"
    :parse-fn #(Integer/parseInt %)]])

(defstate ^{:on-reload :noop} http-server
  :start
  (http/start
   (-> env
       (assoc  :handler #'handler/app)
       (update :io-threads #(or % (* 2 (.availableProcessors (Runtime/getRuntime)))))
       (update :port #(or (-> env :options :port) %))))
  :stop
  (http/stop http-server))

(defstate ^{:on-reload :noop} repl-server
  :start
  (when (env :nrepl-port)
    (nrepl/start {:bind (env :nrepl-bind)
                  :port (env :nrepl-port)}))
  :stop
  (when repl-server
    (nrepl/stop repl-server)))

(defn stop-app []
  (doseq [component (:stopped (mount/stop))]
    (log/info component "stopped"))
  (shutdown-agents))

(defn start-app [args]
  (doseq [component (-> args
                        (parse-opts cli-options)
                        mount/start-with-args
                        :started)]
    (.addShutdownHook (Runtime/getRuntime) (Thread. stop-app))
    (log/info component "started")))

(defn -main [& args]
  (mount/start #'nl.mediquest.zorgrank.config/env)
  (mount/start #'nl.mediquest.zorgrank.sentry/sentry)
  (cond (#{"migrate"} (first args))
        (migrations/migrate args env)
        (:database-url env)
        (do
          (migrations/migrate ["migrate"] env)
          (start-app args))
        :else
        (do
          (log/error "Database configuration not found")
          (log/error ":database-url environment variable must be set before running")
          (System/exit 1))))
