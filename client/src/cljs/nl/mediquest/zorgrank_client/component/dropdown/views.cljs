(ns nl.mediquest.zorgrank-client.component.dropdown.views
  (:require
   [clojure.string :as string]
   [clojure.zip :as zip]
   [nl.mediquest.zorgrank-client.component.dropdown.events]
   [nl.mediquest.zorgrank-client.component.dropdown.plugin.search :as plugin.search]
   [nl.mediquest.zorgrank-client.component.dropdown.subs]
   [re-frame.core :as re-frame]))

(defn- item-selected? [dropdown-name item]
  (when-let [selected @(re-frame/subscribe [:dropdown/selected dropdown-name :selected])]
    (some #{item} selected)))

(defn- render-item-on-click [dropdown-name item]
  (let [on-submit @(re-frame/subscribe [:dropdown/on-submit dropdown-name])]
    (re-frame/dispatch [:dropdown/set-selected dropdown-name item])
    (when (fn? on-submit) (on-submit item))))

(defn- render-item [dropdown-name selected {:keys [value] :as item}]
  [:a.dropdown-item
   {:key (str dropdown-name value)
    :on-click (partial render-item-on-click dropdown-name item)
    :class [(when (item-selected? dropdown-name item) "is-active")
            (when (= (:value selected) value) "is-search-selected")
            (str dropdown-name value)]}
   [:span (:content item)]])

(defn- render-items [dropdown-name]
  (when-let [items @(re-frame/subscribe [:dropdown/items dropdown-name])]
    [:<>
     (doall
      (map (partial render-item dropdown-name (when-not (zip/branch? items) (zip/node items)))
           (zip/root items)))]))

(defn button-title [dropdown-name]
  (->> @(re-frame/subscribe [:dropdown/selected-content dropdown-name])
       (map :content)
       (string/join ", ")))

(defn- button-view [dropdown-name]
  [:div.dropdown-trigger
   [:button.button
    [:span (button-title dropdown-name)]
    [:span.icon.is-size-5
     [:i.fas.fa-angle-down]]]])

(defn- add-is-active-class [props dropdown-name dropdown-id]
  (let [menu-active? (re-frame/subscribe [:dropdown/menu-active? dropdown-name])]
    (update props :class
            #(flatten [% (when @menu-active? :is-active) dropdown-id]))))

(defn- add-has-selected-class [props dropdown-name]
  (if (or (not-empty @(re-frame/subscribe [:dropdown/selected dropdown-name]))
          @(re-frame/subscribe [:dropdown/menu-active? dropdown-name]))
    (update props :class #(flatten [% :has-selected]))
    props))

(defn- add-title-attribute [props dropdown-name]
  (assoc props :title @(re-frame/subscribe [:dropdown/title dropdown-name])))

(defn view [props dropdown-name dropdown-id]
  [:div.dropdown
   (-> props
       (add-is-active-class dropdown-name dropdown-id)
       (add-has-selected-class dropdown-name)
       (add-title-attribute dropdown-name))
   [button-view dropdown-name]
   [:div.dropdown-menu
    [:div.dropdown-content
     (when (and @(re-frame/subscribe [:dropdown/show-search? dropdown-name])
                @(re-frame/subscribe [:dropdown/menu-active? dropdown-name]))
       [plugin.search/view dropdown-name])
     [:hr.dropdown-divider]
     [:div.dropdown-scroll
      (when (empty? @(re-frame/subscribe [:dropdown/items dropdown-name]))
        [:span.dropdown-item "No results found"])
      [render-items dropdown-name]]]]])
