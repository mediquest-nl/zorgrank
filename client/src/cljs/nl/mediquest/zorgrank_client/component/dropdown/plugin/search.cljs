(ns nl.mediquest.zorgrank-client.component.dropdown.plugin.search
  (:require
   [reagent.core :as r]
   [re-frame.core :as re-frame]))

(defn- on-change-search
  [dropdown-name object]
  (re-frame/dispatch [:dropdown/set-search-term
                      dropdown-name
                      (.. object -target -value)]))

(defn- search-submit [dropdown-name event]
  (.preventDefault event)
  (re-frame/dispatch [:dropdown/clear-search-term dropdown-name])
  (re-frame/dispatch [:dropdown/set-selected-from-search-submit dropdown-name])
  (re-frame/dispatch [:dropdown/close-menu dropdown-name]))

(defn- search-view-content [dropdown-name]
  (fn []
    [:form {:on-submit (partial search-submit dropdown-name)}
     [:input.input.dropdown-input
      {:id (str dropdown-name "-search")
       :value @(re-frame/subscribe [:dropdown/search-term dropdown-name])
       :on-change (partial on-change-search dropdown-name)}]]))

(defn- search-keydown-handler [dropdown-name event]
  (let [keycode (.-keyCode event)
        key-down-arrow 40
        key-up-arrow 38]
    (when (#{key-down-arrow key-up-arrow} keycode)
      (.preventDefault event)
      (condp = keycode
        key-down-arrow (re-frame/dispatch [:dropdown/next-item dropdown-name])
        key-up-arrow (re-frame/dispatch [:dropdown/prev-item dropdown-name])))))

(defn view [dropdown-name]
  (let [handler (partial search-keydown-handler dropdown-name)
        dropdown-search (str dropdown-name "-search")]
    (r/create-class
     {:reagent-render (search-view-content dropdown-name)
      :component-will-unmount (fn []
                                (-> dropdown-search
                                    (js/document.getElementById)
                                    (.removeEventListener "keydown" handler)))
      :component-did-mount (fn []
                             (doto (js/document.getElementById dropdown-search)
                               (.focus)
                               (.addEventListener "keydown" handler)))})))
