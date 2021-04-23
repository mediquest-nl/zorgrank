(ns nl.mediquest.zorgrank.middleware.zorgrank-request.scrubber
  "Scrub privacy sensitive information."
  (:require
   [nl.mediquest.zorgrank.util :refer [update-in-when]]))

(defn shorten-geboortedatum
  "Only keep the year part of a geboortedatum. Because a geboortedatum in
  combination with a postcode is personal identifiable information."
  [geboortedatum]
  (when geboortedatum
    (subs geboortedatum 0 4)))

(defn scrub-geboortedatum-in-parameters [request]
  (update-in-when request [:parameters :body :patient :geboortedatum] shorten-geboortedatum))

(defn scrub-geboortedatum-in-body-params [request]
  (update-in-when request [:body-params :patient :geboortedatum] shorten-geboortedatum))

(defn scrub-geboortedatum [request]
  (-> request
      scrub-geboortedatum-in-parameters
      scrub-geboortedatum-in-body-params))

(defn shorten-postcode
  "Only keep the postcode4. Because a geboortedatum in combination with a postcode
  is personal identifiable information."
  [postcode]
  (when postcode
    (subs postcode 0 4)))

(defn scrub-postcode-in-parameters [request]
  (update-in-when request [:parameters :body :patient :postcode] shorten-postcode))

(defn scrub-postcode-in-body-params [request]
  (update-in-when request [:body-params :patient :postcode] shorten-postcode))

(defn scrub-postcode [request]
  (-> request
      scrub-postcode-in-parameters
      scrub-postcode-in-body-params))

(defn wrap-privacy-scrubber
  "Scrubs privacy sensitive information from the request"
  [handler]
  (fn [request]
    (-> request
        scrub-geboortedatum
        scrub-postcode
        handler)))
