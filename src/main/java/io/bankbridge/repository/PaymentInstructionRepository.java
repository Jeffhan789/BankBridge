package io.bankbridge.repository;

import io.bankbridge.domain.PaymentInstruction;
import io.bankbridge.domain.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface PaymentInstructionRepository extends JpaRepository<PaymentInstruction, String> {
    Optional<PaymentInstruction> findByMessageId(String messageId);
    List<PaymentInstruction> findByCreatedAtBetween(Instant start, Instant end);
    long countByStatus(PaymentStatus status);
}
