(ns nl.mediquest.zorgrank-client.component.dropdown.specs
  (:require
   [clojure.spec.alpha :as s]))

(s/def ::dropdown-name string?)

(s/def ::content string?)

(s/def ::title string?)

(s/def ::show-search boolean?)

(s/def ::value any?)

(s/def ::item
  (s/keys :req-un [::content
                   ::value]))

(s/def ::items (s/* ::item))

(s/def ::options
  (s/keys :req-un [::dropdown-name
                   ::items
                   ::title]
          :opt-un [::show-search?]))

(s/fdef init
  :args (s/cat
         :props map?
         :options ::options)
  :ret nil)
