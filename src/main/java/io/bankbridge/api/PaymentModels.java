package io.bankbridge.api;

import io.bankbridge.domain.EntryType;
import io.bankbridge.domain.PaymentStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public final class PaymentModels {
    private PaymentModels() {}

    public record CreatePaymentRequest(
            @NotBlank @Size(max = 64) String messageId,
            @NotBlank @Size(max = 64) String debtorAccount,
            @NotBlank @Size(max = 64) String creditorAccount,
            @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
            @NotBlank @Pattern(regexp = "[A-Za-z]{3}") String currency,
            @NotBlank @Pattern(regexp = "[A-Za-z]{2}") String destinationCountry,
            @NotBlank @Size(max = 32) String purposeCode,
            @NotNull @FutureOrPresent LocalDate requestedExecutionDate
    ) {}

    public record StatusEventView(PaymentStatus status, String reason, Instant occurredAt) {}
    public record ScreeningView(PaymentStatus decision, String matchedRuleId, String reason) {}
    public record LedgerEntryView(String ledgerAccount, EntryType entryType,
                                  BigDecimal amount, String currency) {}
    public record PaymentResponse(
            String id,
            String messageId,
            String debtorAccount,
            String creditorAccount,
            BigDecimal amount,
            String currency,
            String destinationCountry,
            String purposeCode,
            LocalDate requestedExecutionDate,
            PaymentStatus status,
            String statusReason,
            Instant createdAt,
            List<StatusEventView> statusHistory,
            ScreeningView screening,
            List<LedgerEntryView> ledgerEntries
    ) {}
}
