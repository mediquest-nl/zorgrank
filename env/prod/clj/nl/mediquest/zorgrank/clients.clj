(ns nl.mediquest.zorgrank.clients
  (:require
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [clojure.spec.alpha :as s]
   [nl.mediquest.zorgrank.specs :as specs]))

(defn- read-clients-edn
  [clients-edn]
  {:pre [(.exists (io/file clients-edn))]}
  (-> clients-edn
      slurp
      edn/read-string))

(defn- assert-clients [clients]
  (assert (s/valid? ::specs/clients clients)
          (str "Invalid clients in file" (s/explain-data ::specs/clients clients))))

(defn get-clients
  [clients-edn]
  (let [clients (read-clients-edn clients-edn)]
    (assert-clients clients)
    clients))
