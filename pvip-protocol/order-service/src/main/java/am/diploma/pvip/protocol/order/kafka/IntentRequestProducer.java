package am.diploma.pvip.protocol.order.kafka;

import am.diploma.pvip.protocol.order.event.IntentRequestEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class IntentRequestProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishIntentRequest(IntentRequestEvent event) {
        log.info("Publishing intent request: transactionId={}, participant={}", event.getTransactionId(), event.getParticipantType());
        kafkaTemplate.send("pvip.intent-requests", event.getTransactionId().toString(), event);
    }
}
