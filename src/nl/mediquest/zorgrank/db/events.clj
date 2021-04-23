(ns nl.mediquest.zorgrank.db.events
  (:require
   [conman.core :as conman]
   [nl.mediquest.zorgrank.db.core :refer [*db*]]))

(conman/bind-connection *db* "sql/queries/events.sql")
