-- :name delete! :! :n
-- :doc delete all rows
DELETE FROM fct_pc4_reistijd_clean

-- :name add! :! :n
-- :doc add a record
INSERT INTO fct_pc4_reistijd_clean
VALUES (:herkomst_pc, :bestemmings_pc, :reistijd_kortste_minuut, :afstand_kortste_reistijd_km, :afstand_kortste_km)
