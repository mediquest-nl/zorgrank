(ns nl.mediquest.zorgrank-client.utils)

(defn sort-map-by-value [m]
  (into (sorted-map-by #(compare (m %1)
                                 (m %2)))
        m))

(def lookup
  (comp first
        filter))
