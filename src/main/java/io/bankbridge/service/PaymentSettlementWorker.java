package io.bankbridge.service;

import io.bankbridge.domain.*;
import io.bankbridge.messaging.RabbitMqConfig;
import io.bankbridge.messaging.SettlementMessage;
import io.bankbridge.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PaymentSettlementWorker {

    private static final Logger log = LoggerFactory.getLogger(PaymentSettlementWorker.class);

    private final PaymentInstructionRepository paymentRepository;
    private final PaymentStatusEventRepository statusEventRepository;
    private final LedgerEntryRepository ledgerRepository;
    private final AuditEventRepository auditRepository;
    private final ScreeningResultRepository screeningResultRepository;

    public PaymentSettlementWorker(PaymentInstructionRepository paymentRepository,
                                   PaymentStatusEventRepository statusEventRepository,
                                   LedgerEntryRepository ledgerRepository,
                                   AuditEventRepository auditRepository,
                                   ScreeningResultRepository screeningResultRepository) {
        this.paymentRepository = paymentRepository;
        this.statusEventRepository = statusEventRepository;
        this.ledgerRepository = ledgerRepository;
        this.auditRepository = auditRepository;
        this.screeningResultRepository = screeningResultRepository;
    }

    @RabbitListener(queues = RabbitMqConfig.QUEUE)
    @Transactional
    public void handleSettlement(SettlementMessage message) {
        String paymentId = message.paymentId();
        log.info("Received settlement message for payment {}", paymentId);

        PaymentInstruction payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalStateException("Payment not found: " + paymentId));

        // Idempotency check
        if (payment.getStatus() == PaymentStatus.SETTLED || payment.getStatus() == PaymentStatus.FAILED) {
            log.warn("Payment {} already settled/failed, skipping duplicate message", paymentId);
            return;
        }

        try {
            // Transition to PROCESSING
            transition(payment, PaymentStatus.PROCESSING, "Settlement processing started");

            // Create balanced ledger entries
            List<LedgerEntry> entries = List.of(
                    new LedgerEntry(payment.getId(), "CUSTOMER_SETTLEMENT", EntryType.DEBIT,
                            payment.getAmount(), payment.getCurrency()),
                    new LedgerEntry(payment.getId(), "CLEARING_PAYABLE", EntryType.CREDIT,
                            payment.getAmount(), payment.getCurrency()));

            BigDecimal debits = entries.stream()
                    .filter(e -> e.getEntryType() == EntryType.DEBIT)
                    .map(LedgerEntry::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal credits = entries.stream()
                    .filter(e -> e.getEntryType() == EntryType.CREDIT)
                    .map(LedgerEntry::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (debits.compareTo(credits) != 0) {
                transition(payment, PaymentStatus.FAILED, "Ledger entries are not balanced");
                throw new IllegalStateException("Debit and credit totals must be equal");
            }

            ledgerRepository.saveAll(entries);
            auditRepository.save(new AuditEvent("PAYMENT", payment.getId(), "LEDGER_POSTED",
                    "Balanced synthetic ledger entries created", "SYSTEM", "SYSTEM"));

            // Transition to SETTLED
            transition(payment, PaymentStatus.SETTLED, "Synthetic settlement completed");

            log.info("Settlement completed for payment {}", paymentId);
        } catch (Exception e) {
            transition(payment, PaymentStatus.FAILED, e.getMessage());
            log.error("Settlement failed for payment {}: {}", paymentId, e.getMessage(), e);
            throw e;
        }
    }

    private void transition(PaymentInstruction payment, PaymentStatus status, String reason) {
        payment.setStatus(status, reason);
        paymentRepository.save(payment);
        statusEventRepository.save(new PaymentStatusEvent(payment.getId(), status, reason));
        auditRepository.save(new AuditEvent("PAYMENT", payment.getId(), "STATUS_CHANGED",
                status.name() + ": " + reason, "SYSTEM", "SYSTEM"));
    }
        payment.setStatus(status, reason);
        paymentRepository.save(payment);
        statusEventRepository.save(new PaymentStatusEvent(payment.getId(), status, reason));
        auditRepository.save(new AuditEvent("PAYMENT", payment.getId(), "STATUS_CHANGED",
                status.name() + ": " + reason));
    }
}
