(ns nl.mediquest.zorgrank.middleware.authentication
  (:require
   [clojure.spec.alpha :as s]
   [nl.mediquest.zorgrank.specs :as specs]
   [nl.mediquest.zorgrank.specs.authentication :as specs.authn]))

(s/fdef authenticate-request
  :args (s/cat :client-key (s/nilable ::specs/uuid-keyword)
               :clients ::specs/clients
               :authentication-active? boolean?)
  :ret ::specs.authn/authenticated-request)
(defn authenticate-request
  [client-key clients authentication-active?]
  (let [organization (get clients client-key)]
    (when (or organization (not authentication-active?))
      {:client-key (or client-key :00000000-0000-0000-0000-000000000000)
       :authenticated (boolean organization)
       :organization (or organization "anonymous")})))

(defn wrap-authentication
  "Checks whether `client-key` is found in hash-map `clients`.
  Authentication can be bypassed if `authentication-active?` is false."
  ([handler clients]
   (wrap-authentication handler clients true))
  ([handler clients authentication-active?]
   (fn [{:keys [headers] :as request}]
     (let [client-key (-> headers (get "client-key") keyword)]
       (if-let [credentials (authenticate-request client-key clients authentication-active?)]
         (handler (merge request credentials))
         {:status 403
          :headers {"Content-Type" "text/plain"}
          :body "Forbidden"})))))
