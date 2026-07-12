CREATE TABLE outbox_events (
    id VARCHAR(36) PRIMARY KEY,
    aggregate_type VARCHAR(40) NOT NULL,
    aggregate_id VARCHAR(36) NOT NULL,
    event_type VARCHAR(64) NOT NULL,
    payload VARCHAR(4000) NOT NULL,
    published_at TIMESTAMP(6),
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL
);

CREATE INDEX idx_outbox_unpublished ON outbox_events(published_at, created_at);
