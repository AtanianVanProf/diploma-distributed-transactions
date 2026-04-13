package am.diploma.saga.choreography.order.kafka;

import am.diploma.saga.choreography.order.event.OrderCompletedEvent;
import am.diploma.saga.choreography.order.event.OrderFailedEvent;
import am.diploma.saga.choreography.order.event.StockReservationFailedEvent;
import am.diploma.saga.choreography.order.event.StockReservedEvent;
import am.diploma.saga.choreography.order.service.SagaService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@KafkaListener(topics = "inventory-events", groupId = "order-service")
public class InventoryEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(InventoryEventConsumer.class);

    private final SagaService sagaService;

    @KafkaHandler
    public void handleStockReserved(StockReservedEvent event) {
        log.info("Received STOCK_RESERVED for sagaId={}, orderId={}", event.getSagaId(), event.getOrderId());
        sagaService.handleStockReserved(event);
    }

    @KafkaHandler
    public void handleStockReservationFailed(StockReservationFailedEvent event) {
        log.info("Received STOCK_RESERVATION_FAILED for sagaId={}, orderId={}", event.getSagaId(), event.getOrderId());
        sagaService.handleStockReservationFailed(event);
    }

    @KafkaHandler
    public void handleOrderCompleted(OrderCompletedEvent event) {
        log.info("Received ORDER_COMPLETED for sagaId={}, orderId={}", event.getSagaId(), event.getOrderId());
        sagaService.handleOrderCompleted(event);
    }

    @KafkaHandler
    public void handleOrderFailed(OrderFailedEvent event) {
        log.info("Received ORDER_FAILED for sagaId={}, orderId={}", event.getSagaId(), event.getOrderId());
        sagaService.handleOrderFailed(event);
    }

    @KafkaHandler(isDefault = true)
    public void handleDefault(Object event) {
        log.debug("Ignoring event of type {} on inventory-events", event.getClass().getSimpleName());
    }
}
