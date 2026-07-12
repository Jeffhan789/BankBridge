package io.bankbridge.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "outbox_events")
public class OutboxEvent extends BaseEntity {

    @Column(nullable = false, length = 40)
    private String aggregateType;

    @Column(nullable = false, length = 36)
    private String aggregateId;

    @Column(nullable = false, length = 64)
    private String eventType;

    @Column(nullable = false, length = 4000)
    private String payload;

    @Column
    private Instant publishedAt;

    protected OutboxEvent() {}

    public OutboxEvent(String aggregateType, String aggregateId, String eventType, String payload) {
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.payload = payload;
    }

    public String getAggregateType() { return aggregateType; }
    public String getAggregateId() { return aggregateId; }
    public String getEventType() { return eventType; }
    public String getPayload() { return payload; }
    public Instant getPublishedAt() { return publishedAt; }

    public void setPublishedAt(Instant publishedAt) {
        this.publishedAt = publishedAt;
    }
}
