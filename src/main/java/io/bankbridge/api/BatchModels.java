package io.bankbridge.api;

import io.bankbridge.domain.BatchStatus;

import java.time.Instant;

public final class BatchModels {
    private BatchModels() {}
    public record BatchResponse(String id, String fileName, BatchStatus status,
                                int totalRecords, int successfulRecords, int failedRecords,
                                String errorSummary, Instant createdAt, Instant updatedAt) {}
}
