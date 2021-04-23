(ns nl.mediquest.zorgrank.env
  (:require
   [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[nl.mediquest.zorgrank started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[nl.mediquest.zorgrank has shut down successfully]=-"))
   :middleware identity})

(def access-control-allow-origin
  [#"https://zorgrank.*\.mediquest\.cloud"
   #"https://zorgrank.*\.mediquest\.dev"])
