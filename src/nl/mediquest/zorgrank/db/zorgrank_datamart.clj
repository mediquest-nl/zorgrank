(ns nl.mediquest.zorgrank.db.zorgrank-datamart
  (:require
   [conman.core :as conman]
   [hugsql.core :as hugsql]
   [nl.mediquest.zorgrank.db.core :refer [*db*]]))

(hugsql/def-sqlvec-fns "sql/queries/zorgrank_datamart.sql")

(conman/bind-connection *db* "sql/queries/zorgrank_datamart.sql")

;; test
(comment
  (mount.core/start)
  (get-data
   {:specialty "0200"
    :pc4 nil
    :datamart "zorgrank_datamart"}))
