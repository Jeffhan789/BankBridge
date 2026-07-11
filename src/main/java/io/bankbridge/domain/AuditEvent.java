package io.bankbridge.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "audit_events")
public class AuditEvent extends BaseEntity {
    @Column(nullable = false, length = 40)
    private String aggregateType;
    @Column(nullable = false, length = 36)
    private String aggregateId;
    @Column(nullable = false, length = 64)
    private String eventType;
    @Column(nullable = false, length = 1000)
    private String details;

    protected AuditEvent() {}
    public AuditEvent(String aggregateType, String aggregateId, String eventType, String details) {
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.details = details;
    }
    public String getAggregateType() { return aggregateType; }
    public String getAggregateId() { return aggregateId; }
    public String getEventType() { return eventType; }
    public String getDetails() { return details; }
}
