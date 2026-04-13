package am.diploma.saga.choreography.payment.kafka;

import am.diploma.saga.choreography.payment.event.OrderCompletedEvent;
import am.diploma.saga.choreography.payment.event.OrderFailedEvent;
import am.diploma.saga.choreography.payment.event.PaymentChargedEvent;
import am.diploma.saga.choreography.payment.event.PaymentFailedEvent;
import am.diploma.saga.choreography.payment.event.StockReservationFailedEvent;
import am.diploma.saga.choreography.payment.event.StockReservedEvent;
import am.diploma.saga.choreography.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@KafkaListener(topics = "inventory-events", groupId = "payment-service")
public class InventoryEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(InventoryEventConsumer.class);

    private final PaymentService paymentService;
    private final PaymentEventProducer eventProducer;

    @KafkaHandler
    public void handleStockReserved(StockReservedEvent event) {
        log.info("Received STOCK_RESERVED for sagaId={}, orderId={}", event.getSagaId(), event.getOrderId());

        try {
            paymentService.charge(event.getCustomerId(), event.getTotalAmount());

            PaymentChargedEvent chargedEvent = PaymentChargedEvent.builder()
                    .sagaId(event.getSagaId())
                    .orderId(event.getOrderId())
                    .customerId(event.getCustomerId())
                    .amount(event.getTotalAmount())
                    .build();

            eventProducer.publishPaymentCharged(chargedEvent);

        } catch (Exception e) {
            log.error("Payment failed for sagaId={}: {}", event.getSagaId(), e.getMessage());

            PaymentFailedEvent failedEvent = PaymentFailedEvent.builder()
                    .sagaId(event.getSagaId())
                    .orderId(event.getOrderId())
                    .reason(e.getMessage())
                    .build();

            eventProducer.publishPaymentFailed(failedEvent);
        }
    }

    @KafkaHandler
    public void handleStockReservationFailed(StockReservationFailedEvent event) {
        log.debug("Ignoring STOCK_RESERVATION_FAILED for sagaId={} (not relevant to payment)", event.getSagaId());
    }

    @KafkaHandler
    public void handleOrderCompleted(OrderCompletedEvent event) {
        log.debug("Ignoring ORDER_COMPLETED for sagaId={} (not relevant to payment)", event.getSagaId());
    }

    @KafkaHandler
    public void handleOrderFailed(OrderFailedEvent event) {
        log.debug("Ignoring ORDER_FAILED for sagaId={} (not relevant to payment)", event.getSagaId());
    }

    @KafkaHandler(isDefault = true)
    public void handleDefault(Object event) {
        log.debug("Ignoring event of type {} on inventory-events", event.getClass().getSimpleName());
    }
}
