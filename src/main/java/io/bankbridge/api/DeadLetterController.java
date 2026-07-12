package io.bankbridge.api;

import io.bankbridge.messaging.RabbitMqConfig;
import io.bankbridge.messaging.SettlementMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.QueueInformation;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dead-letters")
public class DeadLetterController {

    private static final Logger log = LoggerFactory.getLogger(DeadLetterController.class);

    private final RabbitAdmin rabbitAdmin;
    private final RabbitTemplate rabbitTemplate;

    public DeadLetterController(RabbitAdmin rabbitAdmin, RabbitTemplate rabbitTemplate) {
        this.rabbitAdmin = rabbitAdmin;
        this.rabbitTemplate = rabbitTemplate;
    }

    @GetMapping
    public DeadLetterModels.DeadLetterQueueInfo getDeadLetterQueueInfo() {
        QueueInformation info = rabbitAdmin.getQueueInfo("payment.settlement.dlq");
        long count = info != null ? info.getMessageCount() : 0;
        return new DeadLetterModels.DeadLetterQueueInfo("payment.settlement.dlq", count);
    }

    @PostMapping("/replay")
    public DeadLetterModels.ReplayResponse replayDeadLetters() {
        int replayed = 0;
        while (true) {
            Object obj = rabbitTemplate.receiveAndConvert("payment.settlement.dlq");
            if (obj == null) {
                break;
            }
            if (obj instanceof SettlementMessage msg) {
                rabbitTemplate.convertAndSend(
                        RabbitMqConfig.EXCHANGE,
                        RabbitMqConfig.ROUTING_KEY,
                        msg
                );
                replayed++;
            } else {
                log.warn("Received unexpected message type from DLQ: {}", obj.getClass().getName());
            }
        }
        log.info("Replayed {} messages from dead-letter queue", replayed);
        if (replayed == 0) {
            return new DeadLetterModels.ReplayResponse(false, "No messages in dead-letter queue");
        }
        return new DeadLetterModels.ReplayResponse(true, "Replayed " + replayed + " messages from dead-letter queue");
    }
}
