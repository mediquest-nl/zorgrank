(ns nl.mediquest.zorgrank-client.component.dropdown.utils
  (:require
   [clojure.string :as string]
   [clojure.zip :as zip]
   [nl.mediquest.zorgrank-client.component.dropdown.config :refer [state-name]]))

(defn search-term->words [search-term]
  (-> search-term
      (or "")
      string/lower-case
      (string/split " ")))

(defn contains-every-word? [value words]
  (every? (partial string/includes? (string/lower-case value)) words))

(defn search-item-fn [db dropdown-name content]
  (let [search-term (get-in db [:dropdowns dropdown-name :search-term])]
    (contains-every-word? content (search-term->words search-term))))

(defn item-searched? [db dropdown-name item]
  (search-item-fn db dropdown-name (:content item)))

(defn get-search-selected [db dropdown-name]
  (let [items (get-in db [state-name dropdown-name :items])]
    (when-not (or (zip/branch? items)
                  (zip/end? items))
      (zip/node items))))
