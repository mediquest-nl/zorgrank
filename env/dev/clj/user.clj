(ns user
  (:require
   [clojure.spec.alpha :as s]
   [expound.alpha :as expound]
   [luminus-migrations.core :as migrations]
   [mount.core :as mount]
   [nl.mediquest.zorgrank.config :refer [env *print-sql*]]
   [nl.mediquest.zorgrank.core :refer [start-app]]))

;; (require '[clojure.tools.namespace.repl :refer [refresh-all]])

(alter-var-root #'*print-sql* (constantly true))

(alter-var-root #'s/*explain-out* (constantly expound/printer))

(defn start []
  (mount/start-without #'nl.mediquest.zorgrank.core/repl-server)
  (migrations/migrate ["migrate"] env))

(defn stop []
  (mount/stop-except #'nl.mediquest.zorgrank.core/repl-server))

(defn restart []
  (stop)
  (start))

(defn create-migration [name]
  (migrations/create name env))
