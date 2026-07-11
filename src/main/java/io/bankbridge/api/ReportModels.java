package io.bankbridge.api;

import io.bankbridge.domain.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public final class ReportModels {
    private ReportModels() {}
    public record ReconciliationResponse(LocalDate businessDate, long settledPayments,
                                         BigDecimal debitTotal, BigDecimal creditTotal,
                                         boolean balanced) {}
    public record ComplianceReport(LocalDate businessDate, long totalPayments,
                                   Map<PaymentStatus, Long> statusCounts,
                                   Map<String, BigDecimal> currencyTotals) {}
}
