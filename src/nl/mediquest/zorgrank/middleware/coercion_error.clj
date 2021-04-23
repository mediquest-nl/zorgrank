(ns nl.mediquest.zorgrank.middleware.coercion-error
  (:require
   [clojure.tools.logging :as log]
   [expound.alpha :as expound]))

(defn coercion-error-handler [status]
  (let [printer (expound/custom-printer {:print-specs? false})]
    (fn [exception _request]
      (let [error-msg (with-out-str (printer (-> exception ex-data :problems)))]
        (log/info error-msg)
        {:status status
         :headers {"Content-Type" "text/html"}
         :body error-msg}))))
