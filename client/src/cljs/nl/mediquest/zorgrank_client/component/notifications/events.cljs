(ns nl.mediquest.zorgrank-client.component.notifications.events
  (:require
   [nl.mediquest.zorgrank-client.component.notifications.utils :refer [has-id?]]
   [re-frame.core :as re-frame]))

(re-frame/reg-event-db
 ::add-notification
 (fn [db [_ notification]]
   (update-in db [:component/notifications :notifications/list] conj notification)))

(re-frame/reg-event-db
 ::delete-notification
 (fn [db [_ notification-id]]
   (update-in db [:component/notifications :notifications/list]
              (partial remove (has-id? notification-id)))))
