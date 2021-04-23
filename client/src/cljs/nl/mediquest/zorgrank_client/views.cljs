(ns nl.mediquest.zorgrank-client.views
  (:require
   [nl.mediquest.zorgrank-client.component.footer :as component.footer]
   [nl.mediquest.zorgrank-client.component.header :as component.header]
   [nl.mediquest.zorgrank-client.component.notifications.core :as component.notifications]
   [nl.mediquest.zorgrank-client.page.home :as page.home]
   [nl.mediquest.zorgrank-client.subs]))

(defmethod component.notifications/render :notification/http-error
  [{:notification/keys [content data]}]
  [:div
   [:div content]
   [:div (str data)]])

(defn main-panel []
  [:div
   [component.notifications/init]
   [component.header/init]
   [:div.container.is-fluid
    [page.home/init]]
   [component.footer/init]])
