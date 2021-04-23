(ns nl.mediquest.zorgrank.db.zorgrank-datamart-aandoeningen
  (:require
   [conman.core :as conman]
   [hugsql.core :as hugsql]
   [nl.mediquest.zorgrank.db.core :refer [*db*]]))

(hugsql/def-sqlvec-fns "sql/queries/zorgrank_datamart_aandoeningen.sql")

(conman/bind-connection *db* "sql/queries/zorgrank_datamart_aandoeningen.sql")
