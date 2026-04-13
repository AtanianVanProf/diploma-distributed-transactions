package am.diploma.saga.choreography.inventory.kafka;

import am.diploma.saga.choreography.inventory.event.OrderCompletedEvent;
import am.diploma.saga.choreography.inventory.event.OrderFailedEvent;
import am.diploma.saga.choreography.inventory.event.StockReservationFailedEvent;
import am.diploma.saga.choreography.inventory.event.StockReservedEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InventoryEventProducer {

    private static final Logger log = LoggerFactory.getLogger(InventoryEventProducer.class);
    private static final String TOPIC = "inventory-events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishStockReserved(StockReservedEvent event) {
        log.info("Publishing STOCK_RESERVED for sagaId={}", event.getSagaId());
        kafkaTemplate.send(TOPIC, String.valueOf(event.getSagaId()), event);
    }

    public void publishStockReservationFailed(StockReservationFailedEvent event) {
        log.info("Publishing STOCK_RESERVATION_FAILED for sagaId={}", event.getSagaId());
        kafkaTemplate.send(TOPIC, String.valueOf(event.getSagaId()), event);
    }

    public void publishOrderCompleted(OrderCompletedEvent event) {
        log.info("Publishing ORDER_COMPLETED for sagaId={}", event.getSagaId());
        kafkaTemplate.send(TOPIC, String.valueOf(event.getSagaId()), event);
    }

    public void publishOrderFailed(OrderFailedEvent event) {
        log.info("Publishing ORDER_FAILED for sagaId={}", event.getSagaId());
        kafkaTemplate.send(TOPIC, String.valueOf(event.getSagaId()), event);
    }
}
