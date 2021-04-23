(ns nl.mediquest.zorgrank.config
  (:require
   [cprop.core :refer [load-config]]
   [cprop.source :as source]
   [mount.core :refer [args defstate]]
   [nl.mediquest.zorgrank.clients :as clients]
   [nl.mediquest.zorgrank.util :refer [file->edn]]))

(defn assoc-app-version
  "Version of the app.
  APP_VERSION env variable is added in the container during the deployment step."
  [env]
  (assoc env
         :app-version
         (or (:app-version env) "UNKNOWN")))

(defn assoc-database-url [{:keys [postgres-db-user
                                  postgres-db-password
                                  postgres-db-host
                                  postgres-db-database]
                           :as env}]
  (assoc env
         :database-url
         (format "postgresql://%s:%s@%s/%s"
                 postgres-db-user
                 postgres-db-password
                 postgres-db-host
                 postgres-db-database)))

(defonce ^:dynamic *print-sql* false)

(defstate env
  :start
  (->
   (load-config
    :merge
    [(args)
     (source/from-system-props)
     (source/from-env)])
   (assoc-app-version)
   (assoc-database-url)))

(defstate clients
  :start
  (clients/get-clients (:clients-file env)))

(def languages
  (file->edn "edn/languages.edn"))

(def organisatie-types
  (file->edn "edn/organisatie-types.edn"))

(def adres-soorten
  (file->edn "edn/zibs/adres-soorten.edn"))

(def telecom-typecodes
  (file->edn "edn/zibs/telecom-types.edn"))

(def nummer-soorten
  (file->edn "edn/zibs/nummer-soorten.edn"))

(def email-soorten
  (file->edn "edn/zibs/email-soorten.edn"))

(def datamarts
  (file->edn "edn/datamarts.edn"))

(def zorgcontracteringscore-descriptions
  (file->edn "edn/zorgcontracteringscore-descriptions.edn"))

(def mq-bhc-types
  (file->edn "edn/mq-bhc-types.edn"))

(def vektis-codes
  (file->edn "edn/vektis-codes.edn"))
