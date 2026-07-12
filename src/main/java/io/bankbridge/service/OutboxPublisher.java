package io.bankbridge.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.bankbridge.domain.OutboxEvent;
import io.bankbridge.domain.PaymentInstruction;
import io.bankbridge.messaging.RabbitMqConfig;
import io.bankbridge.messaging.SettlementMessage;
import io.bankbridge.repository.OutboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class OutboxPublisher {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);

    private final OutboxEventRepository outboxRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public OutboxPublisher(OutboxEventRepository outboxRepository,
                           RabbitTemplate rabbitTemplate,
                           ObjectMapper objectMapper) {
        this.outboxRepository = outboxRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void createOutboxEvent(PaymentInstruction payment) {
        try {
            Map<String, Object> payloadMap = new LinkedHashMap<>();
            payloadMap.put("paymentId", payment.getId());
            payloadMap.put("messageId", payment.getMessageId());
            payloadMap.put("debtorAccount", payment.getDebtorAccount());
            payloadMap.put("creditorAccount", payment.getCreditorAccount());
            payloadMap.put("amount", payment.getAmount());
            payloadMap.put("currency", payment.getCurrency());

            String payload = objectMapper.writeValueAsString(payloadMap);

            OutboxEvent event = new OutboxEvent(
                    "PAYMENT",
                    payment.getId(),
                    "SETTLEMENT_REQUESTED",
                    payload
            );
            outboxRepository.save(event);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create outbox event for payment: " + payment.getId(), e);
        }
    }

    @Scheduled(fixedRateString = "${bankbridge.outbox.poll-ms:5000}")
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> pending = outboxRepository.findByPublishedAtIsNullOrderByCreatedAtAsc();
        for (OutboxEvent event : pending) {
            try {
                log.info("Publishing outbox event for payment {}", event.getAggregateId());

                JsonNode node = objectMapper.readTree(event.getPayload());
                SettlementMessage message = new SettlementMessage(
                        node.get("paymentId").asText(),
                        node.get("messageId").asText(),
                        node.get("debtorAccount").asText(),
                        node.get("creditorAccount").asText(),
                        new BigDecimal(node.get("amount").asText()),
                        node.get("currency").asText(),
                        Instant.now()
                );

                rabbitTemplate.convertAndSend(
                        RabbitMqConfig.EXCHANGE,
                        RabbitMqConfig.ROUTING_KEY,
                        message
                );

                event.setPublishedAt(Instant.now());
                outboxRepository.save(event);

                log.info("Published outbox event for payment {}", event.getAggregateId());
            } catch (Exception e) {
                log.error("Failed to publish outbox event for payment {}: {}",
                        event.getAggregateId(), e.getMessage(), e);
            }
        }
    }
}
