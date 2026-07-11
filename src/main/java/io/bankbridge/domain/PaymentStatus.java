package io.bankbridge.domain;

public enum PaymentStatus {
    RECEIVED,
    VALIDATED,
    SCREENING,
    ACCEPTED,
    REJECTED,
    MANUAL_REVIEW,
    PROCESSING,
    SETTLED,
    FAILED
}
