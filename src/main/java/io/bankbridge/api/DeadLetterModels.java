package io.bankbridge.api;

import java.time.Instant;

public final class DeadLetterModels {
    private DeadLetterModels() {}

    public record DeadLetterMessage(
        String messageId,
        String paymentId,
        String reason,
        Instant occurredAt
    ) {}

    public record DeadLetterQueueInfo(
        String queueName,
        long messageCount
    ) {}

    public record ReplayResponse(
        boolean success,
        String message
    ) {}
}
