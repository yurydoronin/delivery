-- Таблица Outbox для интеграционных событий
CREATE TABLE outbox_integration
(
    id              UUID PRIMARY KEY,
    event_type      VARCHAR(255)             NOT NULL,
    aggregate_id    UUID                     NOT NULL,
    aggregate_type  VARCHAR(255)             NOT NULL,
    payload         TEXT                     NOT NULL,
    occurred_on_utc TIMESTAMP WITH TIME ZONE NOT NULL
);
