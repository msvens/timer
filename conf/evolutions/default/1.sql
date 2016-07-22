# --- !Ups

CREATE TABLE timer (
  id SERIAL PRIMARY KEY,
  title VARCHAR(1024) NOT NULL,
  start TIMESTAMP,
  stop TIMESTAMP,
  description TEXT
);

# --- !Downs

DROP TABLE timer;