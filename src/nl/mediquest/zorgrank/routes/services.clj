(ns nl.mediquest.zorgrank.routes.services
  (:require
   [muuntaja.middleware]
   [nl.mediquest.zorgrank.config :refer [clients env]]
   [nl.mediquest.zorgrank.middleware.authentication :refer [wrap-authentication]]
   [nl.mediquest.zorgrank.middleware.cors :as cors]
   [nl.mediquest.zorgrank.middleware.exception :as exception]
   [nl.mediquest.zorgrank.middleware.formats :as formats]
   [nl.mediquest.zorgrank.middleware.formatting :as formatting]
   [nl.mediquest.zorgrank.middleware.logging :as logging]
   [nl.mediquest.zorgrank.middleware.patient-hash-id :refer [wrap-add-patient-hash-id]]
   [nl.mediquest.zorgrank.middleware.zorgrank-request
    :refer
    [wrap-add-scores
     wrap-add-uuid
     wrap-calculate-algorithm
     wrap-get-data-from-zorgrank-datamart
     wrap-send-event
     wrap-valid-choice?]]
   [nl.mediquest.zorgrank.middleware.zorgrank-request.scrubber :refer [wrap-privacy-scrubber]]
   [nl.mediquest.zorgrank.specs.api.zorgrank-choice.request :as specs.zorgrank-choice.request]
   [nl.mediquest.zorgrank.specs.api.zorgrank-choice.response :as specs.zorgrank-choice.response]
   [nl.mediquest.zorgrank.specs.api.zorgrank-request.request :as specs.zorgrank-request.request]
   [nl.mediquest.zorgrank.specs.api.zorgrank-request.response :as specs.zorgrank-request.response]
   [reitit.coercion.spec :as spec-coercion]
   [reitit.ring.coercion :as coercion]
   [reitit.ring.middleware.multipart :as multipart]
   [reitit.ring.middleware.muuntaja :as muuntaja]
   [reitit.ring.middleware.parameters :as parameters]
   [reitit.swagger :as swagger]
   [reitit.swagger-ui :as swagger-ui]
   [ring.util.http-response :refer [ok]]))

(def swagger-description
  "The ZorgRank API is part of the project ‘Gepast Verwijzen’. The project aims
   to help a general practitioner and patient to make joint decisions about the
   most suitable follow-up care by ranking care providers with an arithmetic
   selection model we call ZorgRank. See https://gepastverwijzen.nl/ for more
   information about the project.

   For authorization it's necessary to add your `client-key`
   to the header, click on the button \"Authorize\" to do so in this Swagger UI.
   For testing and demo purposes, you can use the client-key
   00000000-0000-0000-0000-000000000000.

   It is possible to use this API using JSON keys with both underscores (_) and dashes (-).")

(defn service-routes []
  ["/api"
   {:coercion spec-coercion/coercion
    :muuntaja formats/instance
    :swagger {:id ::api}
    :middleware [;; CORS allow access from certain origins
                 cors/cors-middleware
                 ;; log requests and responses
                 logging/logging-middleware
                 ;; query-params & form-params
                 parameters/parameters-middleware
                 ;; content-negotiation
                 muuntaja/format-negotiate-middleware
                 ;; encoding response body
                 muuntaja/format-response-middleware
                 ;; exception handling
                 exception/exception-middleware
                 ;; muuntaja exception handling
                 muuntaja.middleware/wrap-exception
                 ;; decoding request body
                 muuntaja/format-request-middleware
                 ;; convert snake-case to kebab-case
                 formatting/wrap-body-params-to-kebab-case
                 ;; coercing request parameters
                 coercion/coerce-request-middleware
                 ;; multipart
                 multipart/multipart-middleware]}
   ;; swagger documentation
   ["" {:no-doc true
        :swagger {:info {:title "ZorgRank API"
                         :description swagger-description}}}

    ["/swagger.json"
     {:get {:handler (swagger/create-swagger-handler)
            :swagger {:securityDefinitions {:apiKeyAuth
                                            {:type "apiKey"
                                             :name "client-key"
                                             :in "header"}}
                      :security [{:apiKeyAuth []}]}}}]
    ["/api-docs/*"
     {:get (swagger-ui/create-swagger-ui-handler
            {:url "/api/swagger.json"
             :config {:validator-url nil}})}]]

   ["/version"
    {:get (constantly (ok {:app-version (env :app-version)}))}]
   ["/v3"
    {:swagger {:tags ["version 3.0"]}
     :middleware  [[wrap-authentication clients (env :authentication-active?)]]}
    ["/zorgrank-request"
     {:post {:summary "Request for ZorgRank"
             :description (str "This example request describes a referral to ‘Chirurgie’ - "
                               "‘Carpaal tunnelsyndroom’ for a female patient from Utrecht.")
             :middleware [[wrap-privacy-scrubber]
                          [wrap-get-data-from-zorgrank-datamart]
                          [wrap-add-patient-hash-id]
                          [wrap-add-uuid]
                          [wrap-calculate-algorithm]
                          [wrap-add-scores]
                          [formatting/wrap-body-to-snake-case]
                          [coercion/coerce-response-middleware]
                          [wrap-send-event "request"]
                          [wrap-send-event "response"]]
             :parameters {:body ::specs.zorgrank-request.request/zorgrank-request}
             :responses {200 {:body ::specs.zorgrank-request.response/response-body}}
             :handler (fn [request]
                        {:status 200
                         :body {:data (:data request)
                                :zorgrank-id (:uuid request)}})}}]
    ["/zorgrank-choice/:zorgrank-id"
     {:put {:summary "Choose for specialist based on ZorgRank"
            :middleware [[wrap-valid-choice?]
                         [wrap-send-event "request"]]
            :parameters {:path ::specs.zorgrank-choice.request/path-params
                         :body ::specs.zorgrank-choice.request/zorgrank-choice}
            :responses {200 {:body ::specs.zorgrank-choice.response/response-body-200}
                        400 {:body ::specs.zorgrank-choice.response/response-body-40x}
                        404 {:body ::specs.zorgrank-choice.response/response-body-40x}}
            :handler (fn [_request]
                       {:status 200
                        :body nil})}}]]])
