package am.diploma.pvip.protocol.inventory.kafka;

import am.diploma.pvip.protocol.inventory.event.IntentResponseEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class IntentResponseProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String TOPIC = "pvip.intent-responses";

    public void publishIntentResponse(IntentResponseEvent event) {
        log.info("Publishing intent response for transaction={}, status={}", event.getTransactionId(), event.getStatus());
        kafkaTemplate.send(TOPIC, event.getTransactionId().toString(), event);
    }
}
