-- :name add! :! :n
-- :doc add an event
INSERT INTO events
(type, uuid, body)
VALUES (:type, :uuid, :body)

-- :name number-of-results-response :?
-- :doc get number of results last response of request
SELECT jsonb_array_length(body->'data') nr FROM events
WHERE type = :type AND uuid = :zorgrank-id
ORDER BY inserted_at DESC LIMIT 1;
