(ns nl.mediquest.zorgrank-client.component.dropdown.core
  (:require
   [nl.mediquest.zorgrank-client.component.dropdown.specs]
   [nl.mediquest.zorgrank-client.component.dropdown.views :refer [view]]
   [re-frame.core :as re-frame]
   [reagent.core :as r]))

(defn- clicked? [event id]
  (-> event .-target (.closest id)))

(defn- off-click-event-handler [toggle-class off-class dropdown-name event]
  (when-not (clicked? event ".dropdown-input")
    (cond
      (clicked? event (str "." toggle-class))
      (re-frame/dispatch [:dropdown/toggle-menu dropdown-name])
      (not (clicked? event (str "." off-class)))
      (re-frame/dispatch [:dropdown/close-menu dropdown-name]))))

(defn selected [dropdown-name]
  (let [dropdown @(re-frame/subscribe [:dropdown/get dropdown-name])]
    (if (-> dropdown :state :multi-select?)
      (->> dropdown :selected (map :value))
      (->> dropdown :selected first :value))))

(defn init
  ([options] (init {} options))
  ([props {:keys [dropdown-name] :as options}]
   (r/with-let [dropdown-id (str dropdown-name "dropdown")
                handler (partial off-click-event-handler dropdown-id "dropdown-item" dropdown-name)
                _ (js/document.addEventListener "click" handler)]
     (re-frame/dispatch [:dropdown/mount dropdown-name options])
     [view props dropdown-name dropdown-id]
     (finally
       (js/document.removeEventListener "click" handler)))))
