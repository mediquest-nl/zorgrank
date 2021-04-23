(ns nl.mediquest.zorgrank.handler
  (:require
   [mount.core :refer [defstate]]
   [nl.mediquest.zorgrank.env :refer [defaults]]
   [nl.mediquest.zorgrank.middleware :as middleware]
   [nl.mediquest.zorgrank.routes.services :refer [service-routes]]
   [reitit.ring :as ring]
   [ring.middleware.content-type :refer [wrap-content-type]]
   [ring.middleware.webjars :refer [wrap-webjars]]))

(defstate init-app
  :start ((or (:init defaults) (fn [])))
  :stop  ((or (:stop defaults) (fn []))))

(defstate app
  :start
  (middleware/wrap-base
    (ring/ring-handler
      (ring/router
        [["/" {:get
               {:handler
                (constantly
                 {:status 301
                  :headers {"Location" "/api/api-docs/index.html"}})}}]
         (service-routes)])
      (ring/routes
        (ring/create-resource-handler
          {:path "/"})
        (wrap-content-type (wrap-webjars (constantly nil)))
        (ring/create-default-handler)))))
