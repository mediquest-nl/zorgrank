(ns nl.mediquest.zorgrank-client.core
  (:require
   [cljs.spec.test.alpha :as ts]
   [day8.re-frame.http-fx]
   [nl.mediquest.zorgrank-client.config :as config]
   [nl.mediquest.zorgrank-client.events :as events]
   [nl.mediquest.zorgrank-client.views :as views]
   [re-frame.core :as re-frame]
   [reagent.dom :as reagent.dom]))

(defn dev-setup []
  (when config/debug?
    (enable-console-print!)
    (ts/instrument)
    (println "dev mode")))

(defn mount-root []
  (re-frame/clear-subscription-cache!)
  (reagent.dom/render [views/main-panel]
                      (.getElementById js/document "app")))

(defn ^:export init []
  (re-frame/dispatch-sync [::events/initialize-db])
  (dev-setup)
  (mount-root))
