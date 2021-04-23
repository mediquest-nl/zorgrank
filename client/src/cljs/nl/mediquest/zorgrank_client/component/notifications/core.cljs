(ns nl.mediquest.zorgrank-client.component.notifications.core
  (:require
   [clojure.spec.alpha]
   [nl.mediquest.zorgrank-client.component.notifications.specs :as specs]
   [nl.mediquest.zorgrank-client.component.notifications.utils :refer [level->class]]
   [nl.mediquest.zorgrank-client.component.notifications.subs :as subs]
   [nl.mediquest.zorgrank-client.component.notifications.events :as events]
   [spec-signature.core :refer-macros [sdef]]
   [re-frame.core :as re-frame]))

(sdef new-notification [::specs/notification-opts] ::specs/notification)
(defn- new-notification [{:notification/keys [type level content data]}]
  {:notification/id (random-uuid)
   :notification/type type
   :notification/level level
   :notification/content content
   :notification/data data})

(sdef dispatch-notification [::specs/notification] nil?)
(defn- dispatch-notification [notification]
  (re-frame/dispatch [::events/add-notification notification]))

(sdef add-notification [::specs/notification-opts] nil?)
(defn add-notification [notification-opts]
  (-> notification-opts
      (new-notification)
      (dispatch-notification)))

(sdef delete-notification [:notification/id] nil?)
(defn delete-notification [notification-id]
  (re-frame/dispatch [::events/delete-notification notification-id]))

(defmulti render :notification/type)

(defmethod render :default
  [{:notification/keys [content data]}]
  [:div
   [:div content]
   [:div (str data)]])

(defn init []
  [:div.component-notifications
   (doall
    (for [{:notification/keys [id level] :as notification}
          @(re-frame/subscribe [::subs/list])]
      ^{:key id}
      [:div.component.notification
       {:class [(level->class level)]}
       [:button.delete {:on-click #(delete-notification id)}]
       (render notification)]))])
