(ns nl.mediquest.zorgrank.db.postal_code
  (:require
   [conman.core :as conman]
   [nl.mediquest.zorgrank.db.core :refer [*db*]]))

(conman/bind-connection *db* "sql/queries/postal_code.sql")
