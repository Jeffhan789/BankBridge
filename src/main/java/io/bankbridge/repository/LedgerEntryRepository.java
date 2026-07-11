package io.bankbridge.repository;

import io.bankbridge.domain.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, String> {
    List<LedgerEntry> findByPaymentIdOrderByCreatedAtAsc(String paymentId);
    List<LedgerEntry> findByPaymentIdIn(Collection<String> paymentIds);
}
