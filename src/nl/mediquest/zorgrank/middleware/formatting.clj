(ns nl.mediquest.zorgrank.middleware.formatting
  (:require
   [camel-snake-kebab.extras :as cske]
   [clojure.string :as string]))

(defprotocol CaseConverter
  (dash->underscore [value] "Convert all dashes to underscores")
  (underscore->dash [value] "Convert all underscores to dashes"))

(extend-type clojure.lang.Keyword
  CaseConverter
  (dash->underscore [value]
    (-> value
        name
        (string/replace #"-" "_")
        keyword))
  (underscore->dash [value]
    (-> value
        name
        (string/replace #"_" "-")
        keyword)))

(extend-type String
  CaseConverter
  (dash->underscore [value]
    (string/replace value #"-" "_"))
  (underscore->dash [value]
    (string/replace value #"_" "-")))

(extend-type Object
  CaseConverter
  (dash->underscore [value]
    value)
  (underscore->dash [value]
    value))

(defn wrap-body-params-to-kebab-case
  [handler]
  (fn [request]
    (-> request
        (update-in [:body-params] (partial cske/transform-keys underscore->dash))
        handler)))

(defn wrap-body-to-snake-case
  [handler]
  (fn [request]
    (let [response (handler request)]
      (update-in response [:body] (partial cske/transform-keys dash->underscore)))))
