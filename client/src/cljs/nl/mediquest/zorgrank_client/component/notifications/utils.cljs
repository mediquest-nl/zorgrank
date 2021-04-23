(ns nl.mediquest.zorgrank-client.component.notifications.utils
  (:require
   [clojure.spec.alpha]
   [spec-signature.core :refer-macros [sdef]]
   [nl.mediquest.zorgrank-client.component.notifications.specs]))

(sdef has-id? [:notification/id] boolean?)
(defn- has-id? [notification-id]
  (comp some?
        #{notification-id}
        :notification/id))

(def level->class
  {:level/primary :is-primary
   :level/link :is-link
   :level/info :is-info
   :level/success :is-success
   :level/warning :is-warning
   :level/danger :is-danger})
