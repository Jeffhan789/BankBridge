package io.bankbridge.service;

import io.bankbridge.api.ReportModels;
import io.bankbridge.domain.EntryType;
import io.bankbridge.domain.PaymentInstruction;
import io.bankbridge.domain.PaymentStatus;
import io.bankbridge.domain.ReconciliationRecord;
import io.bankbridge.repository.LedgerEntryRepository;
import io.bankbridge.repository.PaymentInstructionRepository;
import io.bankbridge.repository.ReconciliationRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportingService {
    private final PaymentInstructionRepository paymentRepository;
    private final LedgerEntryRepository ledgerRepository;
    private final ReconciliationRecordRepository reconciliationRepository;

    public ReportingService(PaymentInstructionRepository paymentRepository,
                            LedgerEntryRepository ledgerRepository,
                            ReconciliationRecordRepository reconciliationRepository) {
        this.paymentRepository = paymentRepository;
        this.ledgerRepository = ledgerRepository;
        this.reconciliationRepository = reconciliationRepository;
    }

    @Transactional
    public ReportModels.ReconciliationResponse reconcile(LocalDate date) {
        List<PaymentInstruction> settled = paymentsFor(date).stream()
                .filter(payment -> payment.getStatus() == PaymentStatus.SETTLED)
                .toList();
        var entries = ledgerRepository.findByPaymentIdIn(settled.stream().map(PaymentInstruction::getId).toList());
        BigDecimal debits = entries.stream().filter(entry -> entry.getEntryType() == EntryType.DEBIT)
                .map(entry -> entry.getAmount()).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal credits = entries.stream().filter(entry -> entry.getEntryType() == EntryType.CREDIT)
                .map(entry -> entry.getAmount()).reduce(BigDecimal.ZERO, BigDecimal::add);
        ReconciliationRecord record = reconciliationRepository.save(
                new ReconciliationRecord(date, settled.size(), debits, credits));
        return new ReportModels.ReconciliationResponse(record.getBusinessDate(), record.getSettledPayments(),
                record.getDebitTotal(), record.getCreditTotal(), record.isBalanced());
    }

    @Transactional(readOnly = true)
    public ReportModels.ComplianceReport compliance(LocalDate date) {
        List<PaymentInstruction> payments = paymentsFor(date);
        Map<PaymentStatus, Long> statusCounts = new EnumMap<>(PaymentStatus.class);
        Map<String, BigDecimal> currencyTotals = new HashMap<>();
        for (PaymentInstruction payment : payments) {
            statusCounts.merge(payment.getStatus(), 1L, Long::sum);
            currencyTotals.merge(payment.getCurrency(), payment.getAmount(), BigDecimal::add);
        }
        return new ReportModels.ComplianceReport(date, payments.size(), statusCounts, currencyTotals);
    }

    private List<PaymentInstruction> paymentsFor(LocalDate date) {
        Instant start = date.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant end = date.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        return paymentRepository.findByCreatedAtBetween(start, end);
    }
}
