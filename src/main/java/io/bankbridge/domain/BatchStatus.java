package io.bankbridge.domain;

public enum BatchStatus {
    RECEIVED,
    PROCESSING,
    COMPLETED,
    COMPLETED_WITH_ERRORS,
    FAILED
}
