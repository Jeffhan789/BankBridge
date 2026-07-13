package io.bankbridge.service;

import io.bankbridge.api.DuplicateMessageException;
import io.bankbridge.api.BadRequestException;
import io.bankbridge.api.PaymentModels;
import io.bankbridge.api.ResourceNotFoundException;
import io.bankbridge.domain.*;
import io.bankbridge.repository.*;
import io.bankbridge.util.MaskingUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Locale;

@Service
public class PaymentService {
    private final PaymentInstructionRepository paymentRepository;
    private final PaymentStatusEventRepository statusEventRepository;
    private final ScreeningRuleRepository ruleRepository;
    private final ScreeningResultRepository screeningResultRepository;
    private final LedgerEntryRepository ledgerRepository;
    private final AuditEventRepository auditRepository;
    private final OutboxPublisher outboxPublisher;

    public PaymentService(PaymentInstructionRepository paymentRepository,
                          PaymentStatusEventRepository statusEventRepository,
                          ScreeningRuleRepository ruleRepository,
                          ScreeningResultRepository screeningResultRepository,
                          LedgerEntryRepository ledgerRepository,
                          AuditEventRepository auditRepository,
                          OutboxPublisher outboxPublisher) {
        this.paymentRepository = paymentRepository;
        this.statusEventRepository = statusEventRepository;
        this.ruleRepository = ruleRepository;
        this.screeningResultRepository = screeningResultRepository;
        this.ledgerRepository = ledgerRepository;
        this.auditRepository = auditRepository;
        this.outboxPublisher = outboxPublisher;
    }

    @Transactional
    public PaymentModels.PaymentResponse create(PaymentModels.CreatePaymentRequest request) {
        validateBusinessRules(request);
        String messageId = request.messageId().trim();
        paymentRepository.findByMessageId(messageId).ifPresent(existing -> {
            throw new DuplicateMessageException("messageId already exists: " + messageId);
        });

        PaymentInstruction payment = paymentRepository.save(new PaymentInstruction(
                messageId,
                request.debtorAccount().trim(),
                request.creditorAccount().trim(),
                request.amount(),
                request.currency().toUpperCase(Locale.ROOT),
                request.destinationCountry().toUpperCase(Locale.ROOT),
                request.purposeCode().toUpperCase(Locale.ROOT),
                request.requestedExecutionDate()));

        transition(payment, PaymentStatus.RECEIVED, "Payment instruction received");
        transition(payment, PaymentStatus.VALIDATED, "Schema and business fields validated");
        transition(payment, PaymentStatus.SCREENING, "Synthetic compliance screening started");

        ScreeningDecision decision = screen(payment);
        screeningResultRepository.save(new ScreeningResult(payment.getId(), decision.status(),
                decision.ruleId(), decision.reason()));

        if (decision.status() == PaymentStatus.REJECTED || decision.status() == PaymentStatus.MANUAL_REVIEW) {
            transition(payment, decision.status(), decision.reason());
            return response(payment);
        }

        transition(payment, PaymentStatus.ACCEPTED, "Payment accepted for processing");
        outboxPublisher.createOutboxEvent(payment);
        return response(payment);
    }

    private void validateBusinessRules(PaymentModels.CreatePaymentRequest request) {
        if (request.messageId() == null || request.messageId().isBlank())
            throw new BadRequestException("messageId is required");
        if (request.debtorAccount() == null || request.debtorAccount().isBlank())
            throw new BadRequestException("debtorAccount is required");
        if (request.creditorAccount() == null || request.creditorAccount().isBlank())
            throw new BadRequestException("creditorAccount is required");
        if (request.amount() == null || request.amount().compareTo(BigDecimal.ZERO) <= 0)
            throw new BadRequestException("amount must be greater than zero");
        if (request.requestedExecutionDate() == null || request.requestedExecutionDate().isBefore(LocalDate.now()))
            throw new BadRequestException("requestedExecutionDate must be today or later");
        if (request.currency() == null || !request.currency().matches("(?i)[A-Z]{3}"))
            throw new BadRequestException("currency must contain three letters");
        if (request.destinationCountry() == null || !request.destinationCountry().matches("(?i)[A-Z]{2}"))
            throw new BadRequestException("destinationCountry must contain two letters");
        if (request.purposeCode() == null || request.purposeCode().isBlank())
            throw new BadRequestException("purposeCode is required");
        if (request.debtorAccount().equalsIgnoreCase(request.creditorAccount()))
            throw new BadRequestException("debtorAccount and creditorAccount must differ");
    }

    @Transactional(readOnly = true)
    public PaymentModels.PaymentResponse get(String id) {
        return response(paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + id)));
    }

    private ScreeningDecision screen(PaymentInstruction payment) {
        ScreeningRule manualReviewRule = null;
        for (ScreeningRule rule : ruleRepository.findByActiveTrueOrderByCreatedAtAsc()) {
            if (!matches(rule, payment)) continue;
            if (rule.getAction() == ScreeningAction.REJECT) {
                return new ScreeningDecision(PaymentStatus.REJECTED, rule.getId(),
                        "Matched synthetic reject rule: " + rule.getName());
            }
            manualReviewRule = rule;
        }
        if (manualReviewRule != null) {
            return new ScreeningDecision(PaymentStatus.MANUAL_REVIEW, manualReviewRule.getId(),
                    "Matched synthetic manual-review rule: " + manualReviewRule.getName());
        }
        return new ScreeningDecision(PaymentStatus.ACCEPTED, null, "No active synthetic rule matched");
    }

    private boolean matches(ScreeningRule rule, PaymentInstruction payment) {
        String actual = switch (rule.getField()) {
            case DEBTOR_ACCOUNT -> payment.getDebtorAccount();
            case CREDITOR_ACCOUNT -> payment.getCreditorAccount();
            case DESTINATION_COUNTRY -> payment.getDestinationCountry();
            case PURPOSE_CODE -> payment.getPurposeCode();
        };
        return actual.equalsIgnoreCase(rule.getMatchValue());
    }

    private void transition(PaymentInstruction payment, PaymentStatus status, String reason) {
        payment.setStatus(status, reason);
        paymentRepository.save(payment);
        statusEventRepository.save(new PaymentStatusEvent(payment.getId(), status, reason));
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String actorUsername = auth != null ? auth.getName() : null;
        String actorRole = auth != null && !auth.getAuthorities().isEmpty()
                ? auth.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "")
                : null;
        auditRepository.save(new AuditEvent("PAYMENT", payment.getId(), "STATUS_CHANGED",
                status.name() + ": " + reason, actorUsername, actorRole));
    }

    private PaymentModels.PaymentResponse response(PaymentInstruction payment) {
        var history = statusEventRepository.findByPaymentIdOrderByCreatedAtAsc(payment.getId()).stream()
                .map(event -> new PaymentModels.StatusEventView(event.getStatus(), event.getReason(), event.getCreatedAt()))
                .toList();
        var screening = screeningResultRepository.findFirstByPaymentIdOrderByCreatedAtDesc(payment.getId())
                .map(result -> new PaymentModels.ScreeningView(result.getDecision(),
                        result.getMatchedRuleId(), result.getReason()))
                .orElse(null);
        var ledger = ledgerRepository.findByPaymentIdOrderByCreatedAtAsc(payment.getId()).stream()
                .map(entry -> new PaymentModels.LedgerEntryView(entry.getLedgerAccount(),
                        entry.getEntryType(), entry.getAmount(), entry.getCurrency()))
                .toList();

        boolean isAdmin = isCurrentUserAdmin();
        String debtorAccount = isAdmin ? payment.getDebtorAccount() : MaskingUtils.maskAccount(payment.getDebtorAccount());
        String creditorAccount = isAdmin ? payment.getCreditorAccount() : MaskingUtils.maskAccount(payment.getCreditorAccount());

        return new PaymentModels.PaymentResponse(payment.getId(), payment.getMessageId(),
                debtorAccount, creditorAccount, payment.getAmount(),
                payment.getCurrency(), payment.getDestinationCountry(), payment.getPurposeCode(),
                payment.getRequestedExecutionDate(), payment.getStatus(), payment.getStatusReason(),
                payment.getCreatedAt(), history, screening, ledger);
    }

    private boolean isCurrentUserAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    private record ScreeningDecision(PaymentStatus status, String ruleId, String reason) {}
}
