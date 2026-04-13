package am.diploma.saga.choreography.order.kafka;

import am.diploma.saga.choreography.order.event.OrderPlacedEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderEventProducer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventProducer.class);
    private static final String TOPIC = "order-events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishOrderPlaced(OrderPlacedEvent event) {
        log.info("Publishing ORDER_PLACED for sagaId={}", event.getSagaId());
        kafkaTemplate.send(TOPIC, String.valueOf(event.getSagaId()), event);
    }
}
