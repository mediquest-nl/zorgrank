(ns nl.mediquest.zorgrank.middleware.cors
  (:require
   [nl.mediquest.zorgrank.env :refer [access-control-allow-origin]]
   [ring.middleware.cors :as ring.middleware]))

(defn cors-middleware
  [handler]
  (ring.middleware/wrap-cors handler
                             :access-control-allow-origin access-control-allow-origin
                             :access-control-allow-methods [:get :post]))
