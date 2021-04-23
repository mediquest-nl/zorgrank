(ns nl.mediquest.zorgrank.middleware.uuid
  (:import
   (java.util UUID)))

(defn add-uuid
  "Add uuid to request"
  [handler]
  (fn [request]
    (let [uuid (UUID/randomUUID)]
      (handler (assoc request :uuid uuid)))))

;; test
(comment
  ((add-uuid identity) {}))
