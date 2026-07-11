package io.bankbridge.repository;

import io.bankbridge.domain.ReconciliationRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReconciliationRecordRepository extends JpaRepository<ReconciliationRecord, String> {}
