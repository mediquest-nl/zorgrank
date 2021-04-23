CREATE TABLE events (
  id SERIAL PRIMARY KEY,
  uuid TEXT NOT NULL,
  type TEXT NOT NULL,
  body JSONB NOT NULL,
  inserted_at TIMESTAMP(6) NOT NULL DEFAULT statement_timestamp()
);
