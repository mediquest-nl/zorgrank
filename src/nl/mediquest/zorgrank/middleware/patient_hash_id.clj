(ns nl.mediquest.zorgrank.middleware.patient-hash-id
  (:require
   [clojure.string :as string])
  (:import
   (clojure.lang PersistentArrayMap)
   (java.security MessageDigest)))

(defn- md5 [^String s]
  (let [algorithm (MessageDigest/getInstance "MD5")
        raw (.digest algorithm (.getBytes s))]
    (format "%032x" (BigInteger. 1 raw))))

(defn- hash-patient [^PersistentArrayMap patient ^String remote-addr]
  (md5 (str
        (string/join (vals patient))
        remote-addr)))

(defn wrap-add-patient-hash-id
  [handler]
  (fn [{:keys [body-params remote-addr] :as request}]
    (let [patient (:patient body-params)
          patient-id (hash-patient patient remote-addr)]
      (handler (assoc request :patient-id patient-id)))))

;; test
(comment
  ((add-patient-hash-id identity) {:remote-addr 123 :body-params {:patient {:b 1}}}))
