CREATE INDEX IF NOT EXISTS events_uuid_idx ON events (uuid)
WITH (FILLFACTOR = 50);
