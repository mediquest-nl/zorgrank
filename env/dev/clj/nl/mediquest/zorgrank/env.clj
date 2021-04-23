(ns nl.mediquest.zorgrank.env
  (:require
   [clojure.spec.test.alpha :as stest]
   [clojure.tools.logging :as log]
   [nl.mediquest.zorgrank.dev-middleware :refer [wrap-dev]]
   [selmer.parser :as parser]))

(def defaults
  {:init
   (fn []
     (stest/instrument)
     (parser/cache-off!)
     (log/info "\n-=[nl.mediquest.zorgrank started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[nl.mediquest.zorgrank has shut down successfully]=-"))
   :middleware wrap-dev})

(def access-control-allow-origin
  [#"http://localhost:3449"])
