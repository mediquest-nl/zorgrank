(ns nl.mediquest.zorgrank.middleware
  (:require
   [nl.mediquest.zorgrank.env :refer [defaults]]
   [ring-ttl-session.core :refer [ttl-memory-store]]
   [ring.middleware.defaults :refer [site-defaults wrap-defaults]]))

(defn wrap-base [handler]
  (-> ((:middleware defaults) handler)
      (wrap-defaults
       (-> site-defaults
           (assoc-in [:security :anti-forgery] false)
           (assoc-in [:session :store] (ttl-memory-store (* 60 30)))))))
