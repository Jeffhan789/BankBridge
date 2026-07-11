package io.bankbridge.repository;

import io.bankbridge.domain.ScreeningResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ScreeningResultRepository extends JpaRepository<ScreeningResult, String> {
    Optional<ScreeningResult> findFirstByPaymentIdOrderByCreatedAtDesc(String paymentId);
}
