package io.bankbridge.repository;

import io.bankbridge.domain.AuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditEventRepository extends JpaRepository<AuditEvent, String> {
    List<AuditEvent> findByAggregateIdOrderByCreatedAtAsc(String aggregateId);
    List<AuditEvent> findTop100ByOrderByCreatedAtDesc();
}
