package am.diploma.pvip.protocol.commitgate.kafka;

import am.diploma.pvip.protocol.commitgate.event.IntentResponseEvent;
import am.diploma.pvip.protocol.commitgate.service.CommitGateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class IntentResponseConsumer {

    private final CommitGateService commitGateService;

    @KafkaListener(topics = "pvip.intent-responses", groupId = "commitgate-service", containerFactory = "kafkaListenerContainerFactory")
    public void consume(IntentResponseEvent event) {
        try {
            commitGateService.processIntentResponse(event);
        } catch (Exception e) {
            log.error("Failed to process intent response for transaction={}: {}",
                    event.getTransactionId(), e.getMessage(), e);
        }
    }
}
