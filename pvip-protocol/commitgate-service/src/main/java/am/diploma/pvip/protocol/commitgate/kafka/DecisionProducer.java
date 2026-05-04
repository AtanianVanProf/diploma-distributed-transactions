package am.diploma.pvip.protocol.commitgate.kafka;

import am.diploma.pvip.protocol.commitgate.event.GlobalDecisionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DecisionProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishDecision(GlobalDecisionEvent event) {
        log.info("Publishing decision {} for transaction={}", event.getDecision(), event.getTransactionId());
        kafkaTemplate.send("pvip.decisions", event.getTransactionId().toString(), event);
    }
}
