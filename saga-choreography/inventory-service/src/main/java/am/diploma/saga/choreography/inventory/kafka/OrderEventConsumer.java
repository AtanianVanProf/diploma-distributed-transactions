package am.diploma.saga.choreography.inventory.kafka;

import am.diploma.saga.choreography.inventory.event.OrderPlacedEvent;
import am.diploma.saga.choreography.inventory.event.StockReservationFailedEvent;
import am.diploma.saga.choreography.inventory.event.StockReservedEvent;
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
@KafkaListener(topics = "order-events", groupId = "inventory-service")
public class OrderEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventConsumer.class);

    private final InventoryService inventoryService;
    private final InventoryEventProducer eventProducer;
    private final ReservationDataStore reservationDataStore;

    @KafkaHandler
    public void handleOrderPlaced(OrderPlacedEvent event) {
        log.info("Received ORDER_PLACED for sagaId={}, orderId={}", event.getSagaId(), event.getOrderId());

        try {
            List<InventoryService.StockItemData> stockItems = event.getItems().stream()
                    .map(item -> new InventoryService.StockItemData(item.getProductId(), item.getQuantity()))
                    .toList();

            InventoryService.ReservationResult result = inventoryService.reserveStock(stockItems);

            List<StockReservedEvent.ReservedItem> reservedItems = result.items().stream()
                    .map(item -> StockReservedEvent.ReservedItem.builder()
                            .productId(item.productId())
                            .productName(item.productName())
                            .quantity(item.quantity())
                            .price(item.price())
                            .build())
                    .toList();

            reservationDataStore.save(event.getSagaId(),
                    new ReservationDataStore.ReservationData(
                            event.getCustomerId(), reservedItems, result.totalAmount()));

            StockReservedEvent stockReservedEvent = StockReservedEvent.builder()
                    .sagaId(event.getSagaId())
                    .orderId(event.getOrderId())
                    .customerId(event.getCustomerId())
                    .items(reservedItems)
                    .totalAmount(result.totalAmount())
                    .build();

            eventProducer.publishStockReserved(stockReservedEvent);

        } catch (Exception e) {
            log.error("Stock reservation failed for sagaId={}: {}", event.getSagaId(), e.getMessage());

            StockReservationFailedEvent failedEvent = StockReservationFailedEvent.builder()
                    .sagaId(event.getSagaId())
                    .orderId(event.getOrderId())
                    .reason(e.getMessage())
                    .build();

            eventProducer.publishStockReservationFailed(failedEvent);
        }
    }
}
