(ns nl.mediquest.zorgrank-client.component.dropdown.subs
  (:require
   [nl.mediquest.zorgrank-client.component.dropdown.config :refer [state-name]]
   [nl.mediquest.zorgrank-client.component.dropdown.utils :refer [get-search-selected]]
   [re-frame.core :as re-frame]))

(re-frame/reg-sub
 :dropdown/selected
 (fn [db [_ dropdown-name]]
   (get-in db [state-name dropdown-name :selected])))

(re-frame/reg-sub
 :dropdown/selected-content
 (fn [db [_ dropdown-name]]
   (get-in db [state-name dropdown-name :selected])))

(re-frame/reg-sub
 :dropdown/search-term
 (fn [db [_ dropdown-name]]
   (get-in db [state-name dropdown-name :search-term])))

(re-frame/reg-sub
 :dropdown/menu-active?
 (fn [db [_ dropdown-name]]
   (get-in db [state-name dropdown-name :menu-active?])))

(re-frame/reg-sub
 :dropdown/title
 (fn [db [_ dropdown-name]]
   (get-in db [state-name dropdown-name :state :title])))

(re-frame/reg-sub
 :dropdown/items
 (fn [db [_ dropdown-name]]
   (get-in db [state-name dropdown-name :items])))

(re-frame/reg-sub
 :dropdown/show-search?
 (fn [db [_ dropdown-name]]
   (get-in db [state-name dropdown-name :state :show-search?])))

(re-frame/reg-sub
 :dropdown/on-submit
 (fn [db [_ dropdown-name]]
   (get-in db [state-name dropdown-name :state :on-submit])))

(re-frame/reg-sub
 :dropdown/search-selected
 (fn [db [_ dropdown-name]]
   (get-search-selected db dropdown-name)))

(re-frame/reg-sub
 :dropdown/get
 (fn [db [_ dropdown-name]]
   (get-in db [state-name dropdown-name])))
