package io.bankbridge.repository;

import io.bankbridge.domain.BatchJob;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BatchJobRepository extends JpaRepository<BatchJob, String> {}
