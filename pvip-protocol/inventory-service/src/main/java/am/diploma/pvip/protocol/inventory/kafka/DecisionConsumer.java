package am.diploma.pvip.protocol.inventory.kafka;

import am.diploma.pvip.protocol.inventory.event.GlobalDecisionEvent;
import am.diploma.pvip.protocol.inventory.service.IntentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DecisionConsumer {

    private final IntentService intentService;

    @KafkaListener(topics = "pvip.decisions", groupId = "inventory-service", containerFactory = "kafkaListenerContainerFactory")
    public void consume(GlobalDecisionEvent event) {
        log.info("Received global decision for transaction={}, decision={}", event.getTransactionId(), event.getDecision());

        try {
            switch (event.getDecision()) {
                case "COMMIT" -> {
                    intentService.finalizeIntent(event.getTransactionId());
                    log.info("Transaction {} committed successfully", event.getTransactionId());
                }
                case "REJECT" -> {
                    intentService.cancelIntent(event.getTransactionId());
                    log.info("Transaction {} cancelled", event.getTransactionId());
                }
                default -> log.warn("Unknown decision type: {} for transaction={}", event.getDecision(), event.getTransactionId());
            }
        } catch (Exception e) {
            log.error("Failed to process decision for transaction={}: {}", event.getTransactionId(), e.getMessage(), e);
        }
    }
}
