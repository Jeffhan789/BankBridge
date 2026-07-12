package io.bankbridge.repository;

import io.bankbridge.domain.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, String> {

    List<OutboxEvent> findByPublishedAtIsNullOrderByCreatedAtAsc();

    Optional<OutboxEvent> findByAggregateIdAndEventType(String aggregateId, String eventType);
}
