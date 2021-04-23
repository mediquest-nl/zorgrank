(ns nl.mediquest.zorgrank-client.component.notifications.specs
  (:require [clojure.spec.alpha :as s]))

(s/def :notification/id uuid?)
(s/def :notification/type keyword?)
(s/def :notification/content string?)
(s/def :notification/data map?)
(s/def :notification/level
  #{:level/primary
    :level/link
    :level/info
    :level/success
    :level/warning
    :level/danger})

(s/def ::notification
  (s/keys :req [:notification/id
                :notification/type
                :notification/content
                :notification/data
                :notification/level]))

(s/def ::notification-opts
  (s/keys :req [:notification/type
                :notification/content
                :notification/data
                :notification/level]))
