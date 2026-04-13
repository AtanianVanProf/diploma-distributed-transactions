package am.diploma.saga.choreography.payment.kafka;

import am.diploma.saga.choreography.payment.event.PaymentChargedEvent;
import am.diploma.saga.choreography.payment.event.PaymentFailedEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentEventProducer {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventProducer.class);
    private static final String TOPIC = "payment-events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishPaymentCharged(PaymentChargedEvent event) {
        log.info("Publishing PAYMENT_CHARGED for sagaId={}", event.getSagaId());
        kafkaTemplate.send(TOPIC, String.valueOf(event.getSagaId()), event);
    }

    public void publishPaymentFailed(PaymentFailedEvent event) {
        log.info("Publishing PAYMENT_FAILED for sagaId={}", event.getSagaId());
        kafkaTemplate.send(TOPIC, String.valueOf(event.getSagaId()), event);
    }
}
