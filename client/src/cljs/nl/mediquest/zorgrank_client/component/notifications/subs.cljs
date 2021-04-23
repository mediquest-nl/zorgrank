(ns nl.mediquest.zorgrank-client.component.notifications.subs
  (:require
   [nl.mediquest.zorgrank-client.component.notifications.utils :refer [has-id?]]
   [re-frame.core :as re-frame]))

(re-frame/reg-sub
 ::list
 (comp :notifications/list
       :component/notifications))
