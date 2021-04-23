(ns nl.mediquest.zorgrank-client.db)

(def default-db
  {:slider-kwaliteit 1
   :slider-afstand 1
   :slider-wachttijd 1
   :filter-kwaliteit nil
   :filter-afstand nil
   :filter-wachttijd nil
   :postcode nil
   :specialisme-code nil
   :auth/client-key "00000000-0000-0000-0000-000000000000"
   :mouse-down false
   :zorgrank-response []
   :specialist/selected nil
   :specialist/confirmed nil
   :notifications []})
