(ns nl.mediquest.zorgrank-client.component.dropdown.events
  (:require
   [clojure.spec.alpha :as s]
   [clojure.zip :as zip]
   [nl.mediquest.zorgrank-client.component.dropdown.config :refer [state-name]]
   [nl.mediquest.zorgrank-client.component.dropdown.specs :as specs]
   [nl.mediquest.zorgrank-client.component.dropdown.utils
    :refer [item-searched? get-search-selected]]
   [re-frame.core :as re-frame :refer [after]]
   [re-frame.interop :refer [debug-enabled?]]))

(def interceptor-check-options
  (when debug-enabled?
    (after
     (fn [_db [_ _dropdown-name props]]
       (when-not (s/valid? ::specs/options props)
         (throw (ex-info (str "spec check failed: " (s/explain-str ::specs/options props))
                         {})))))))

(defn create-zip-items [items]
  (if (empty? items)
    (-> items vec zip/vector-zip)
    (-> items vec zip/vector-zip zip/next)))

(defn db->dropdown-zip [dropdown-name db]
  (create-zip-items
   (filter (partial item-searched? db dropdown-name)
           (get-in db [state-name dropdown-name :state :items]))))

(defn multi-select? [db dropdown-name]
  (get-in db [state-name dropdown-name :state :multi-select?] false))

(defn selected [db dropdown-name]
  (get-in db [state-name dropdown-name :selected] #{}))

(def clear-selection?
  (comp nil?
        :value))

(defn add-item [db dropdown-name item]
  (cond
    (clear-selection? item)
    (assoc-in db [state-name dropdown-name :selected] #{})

    (multi-select? db dropdown-name)
    (let [items (selected db dropdown-name)]
      (if (items item)
        (update-in db [state-name dropdown-name :selected] disj item)
        (update-in db [state-name dropdown-name :selected] conj item)))

    :else
    (assoc-in db [state-name dropdown-name :selected] #{item})))

(re-frame/reg-event-db
 :dropdown/mount
 [interceptor-check-options]
 (fn [db [_ dropdown-name {:keys [items] :as state}]]
   (-> db
       (assoc-in [state-name dropdown-name :state] state)
       (assoc-in [state-name dropdown-name :items] (create-zip-items items))
       (assoc-in [state-name dropdown-name :selected] #{}))))

(re-frame/reg-event-db
 :dropdown/set-selected
 (fn [db [_ dropdown-name value]]
   (-> db
       (assoc-in [state-name dropdown-name :search-term] nil)
       (add-item dropdown-name value))))

(re-frame/reg-event-db
 :dropdown/set-selected-from-search-submit
 (fn [db [_ dropdown-name]]
   (let [selected (get-search-selected db dropdown-name)
         items (get-in db [state-name dropdown-name :state :items])]
     (-> db
         (add-item dropdown-name selected)
         (assoc-in [state-name dropdown-name :items] (create-zip-items items))))))

(re-frame/reg-event-db
 :dropdown/set-search-term
 (fn [db [_ dropdown-name search-term]]
   (let [db (assoc-in db [state-name dropdown-name :search-term] search-term)]
     (assoc-in db [state-name dropdown-name :items] (db->dropdown-zip dropdown-name db)))))

(re-frame/reg-event-db
 :dropdown/clear-search-term
 (fn [db [_ dropdown-name]]
   (assoc-in db [state-name dropdown-name :search-term] nil)))

(re-frame/reg-event-db
 :dropdown/toggle-menu
 (fn [db [_ dropdown-name]]
   (update-in db [state-name dropdown-name :menu-active?] not)))

(re-frame/reg-event-db
 :dropdown/close-menu
 (fn [db [_ dropdown-name]]
   (assoc-in db [state-name dropdown-name :menu-active?] false)))

(defn scroll-to-element! [element]
  (set! (.. element -parentNode -scrollTop)
        (- (.-offsetTop element)
           (.. element -parentNode -offsetTop))))

(defn get-element [dropdown-name value]
  (when-let [elements (array-seq (js/document.getElementsByClassName (str dropdown-name value)))]
    (first elements)))

(defn next-search-item [items]
  (if (zip/end? (zip/next items))
    items
    (zip/next items)))

(defn prev-search-item [items]
  (if (zip/branch? (zip/prev items))
    items
    (zip/prev items)))

(re-frame/reg-fx
 :scroll-to-element
 (fn [{:keys [dropdown-name value]}]
   (-> dropdown-name
       (get-element value)
       scroll-to-element!)))

(defn scroll-item-cofx [db dropdown-name scroll-fn]
  (let [db (update-in db [state-name dropdown-name :items] scroll-fn)
        value (:value (get-search-selected db dropdown-name))]
    {:db db
     :scroll-to-element {:dropdown-name dropdown-name
                         :value value}}))

(re-frame/reg-event-fx
 :dropdown/next-item
 (fn [{:keys [db]} [_ dropdown-name]]
   (scroll-item-cofx db dropdown-name next-search-item)))

(re-frame/reg-event-fx
 :dropdown/prev-item
 (fn [{:keys [db]} [_ dropdown-name]]
   (scroll-item-cofx db dropdown-name prev-search-item)))
