package am.diploma.saga.choreography.inventory.kafka;

import am.diploma.saga.choreography.inventory.event.OrderCompletedEvent;
import am.diploma.saga.choreography.inventory.event.OrderFailedEvent;
import am.diploma.saga.choreography.inventory.event.PaymentChargedEvent;
import am.diploma.saga.choreography.inventory.event.PaymentFailedEvent;
import am.diploma.saga.choreography.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@KafkaListener(topics = "payment-events", groupId = "inventory-service")
public class PaymentEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventConsumer.class);

    private final InventoryService inventoryService;
    private final InventoryEventProducer eventProducer;
    private final ReservationDataStore reservationDataStore;

    @KafkaHandler
    public void handlePaymentCharged(PaymentChargedEvent event) {
        log.info("Received PAYMENT_CHARGED for sagaId={}, orderId={}", event.getSagaId(), event.getOrderId());

        ReservationDataStore.ReservationData reservationData = reservationDataStore.retrieve(event.getSagaId());

        if (reservationData == null) {
            log.error("No reservation data found for sagaId={}", event.getSagaId());
            return;
        }

        List<OrderCompletedEvent.CompletedItem> completedItems = reservationData.items().stream()
                .map(item -> OrderCompletedEvent.CompletedItem.builder()
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .build())
                .toList();

        OrderCompletedEvent completedEvent = OrderCompletedEvent.builder()
                .sagaId(event.getSagaId())
                .orderId(event.getOrderId())
                .customerId(event.getCustomerId())
                .items(completedItems)
                .totalAmount(reservationData.totalAmount())
                .build();

        eventProducer.publishOrderCompleted(completedEvent);
    }

    @KafkaHandler
    public void handlePaymentFailed(PaymentFailedEvent event) {
        log.info("Received PAYMENT_FAILED for sagaId={}, orderId={}", event.getSagaId(), event.getOrderId());

        ReservationDataStore.ReservationData reservationData = reservationDataStore.retrieve(event.getSagaId());

        if (reservationData != null) {
            // Compensation: release reserved stock
            List<InventoryService.StockItemData> stockItems = reservationData.items().stream()
                    .map(item -> new InventoryService.StockItemData(item.getProductId(), item.getQuantity()))
                    .toList();

            inventoryService.releaseStock(stockItems);
            log.info("Stock released (compensation) for sagaId={}", event.getSagaId());
        } else {
            log.warn("No reservation data found for sagaId={} during compensation", event.getSagaId());
        }

        OrderFailedEvent failedEvent = OrderFailedEvent.builder()
                .sagaId(event.getSagaId())
                .orderId(event.getOrderId())
                .reason(event.getReason())
                .build();

        eventProducer.publishOrderFailed(failedEvent);
    }
}
