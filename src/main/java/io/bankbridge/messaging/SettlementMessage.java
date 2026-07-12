package io.bankbridge.messaging;

import java.math.BigDecimal;
import java.time.Instant;

public record SettlementMessage(
    String paymentId,
    String messageId,
    String debtorAccount,
    String creditorAccount,
    BigDecimal amount,
    String currency,
    Instant occurredAt
) {}
