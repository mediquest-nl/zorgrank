(ns nl.mediquest.zorgrank.middleware.exception
  (:require
   [nl.mediquest.zorgrank.middleware.coercion-error
    :refer [coercion-error-handler]]
   [nl.mediquest.zorgrank.middleware.sentry
    :refer [sentry-error-handler]]
   [reitit.coercion :as coercion]
   [reitit.ring.middleware.exception :as exception]))

(def exception-middleware
  (exception/create-exception-middleware
   (merge
    exception/default-handlers
    {::coercion/request-coercion (coercion-error-handler 400)
     ::coercion/response-coercion (coercion-error-handler 500)
     ::exception/default sentry-error-handler})))
