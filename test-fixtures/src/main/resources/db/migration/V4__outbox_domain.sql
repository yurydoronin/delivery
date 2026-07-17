-- Таблица Outbox для доменных событий
CREATE TABLE outbox_domain
(
    id               UUID PRIMARY KEY,
    event_type       VARCHAR(255)             NOT NULL,
    aggregate_id     UUID                     NOT NULL,
    aggregate_type   VARCHAR(255)             NOT NULL,
    payload          TEXT                     NOT NULL,
    occurred_on_utc  TIMESTAMP WITH TIME ZONE NOT NULL,
    processed_on_utc TIMESTAMP WITH TIME ZONE DEFAULT NULL
);
