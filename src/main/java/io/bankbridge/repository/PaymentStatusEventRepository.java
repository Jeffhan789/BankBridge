package io.bankbridge.repository;

import io.bankbridge.domain.PaymentStatusEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentStatusEventRepository extends JpaRepository<PaymentStatusEvent, String> {
    List<PaymentStatusEvent> findByPaymentIdOrderByCreatedAtAsc(String paymentId);
}
