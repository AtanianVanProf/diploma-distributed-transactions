package am.diploma.pvip.protocol.inventory.kafka;

import am.diploma.pvip.protocol.inventory.entity.Product;
import am.diploma.pvip.protocol.inventory.entity.TransactionIntent;
import am.diploma.pvip.protocol.inventory.event.IntentRequestEvent;
import am.diploma.pvip.protocol.inventory.event.IntentResponseEvent;
import am.diploma.pvip.protocol.inventory.service.IntentService;
import am.diploma.pvip.protocol.inventory.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class IntentRequestConsumer {

    private final IntentService intentService;
    private final ProductService productService;
    private final IntentResponseProducer intentResponseProducer;

    @KafkaListener(topics = "pvip.intent-requests", groupId = "inventory-service", containerFactory = "kafkaListenerContainerFactory")
    public void consume(IntentRequestEvent event) {
        if (!"INVENTORY".equals(event.getParticipantType())) {
            log.debug("Ignoring intent request for participantType={}", event.getParticipantType());
            return;
        }

        log.info("Received intent request for transaction={}, product={}, quantity={}",
                event.getTransactionId(), event.getProductId(), event.getQuantity());

        try {
            TransactionIntent intent = intentService.registerIntent(
                    event.getTransactionId(), event.getProductId(), event.getQuantity());

            Product product = productService.getProduct(intent.getProductId());

            IntentResponseEvent response = IntentResponseEvent.builder()
                    .transactionId(intent.getTransactionId())
                    .participantType("INVENTORY")
                    .status(intent.getStatus().name())
                    .reason(intent.getReason())
                    .productId(intent.getProductId())
                    .productName(product.getName())
                    .availableStock(product.getStock())
                    .price(product.getPrice())
                    .build();

            intentResponseProducer.publishIntentResponse(response);
        } catch (Exception e) {
            log.error("Failed to process intent request for transaction={}: {}", event.getTransactionId(), e.getMessage());

            IntentResponseEvent response = IntentResponseEvent.builder()
                    .transactionId(event.getTransactionId())
                    .participantType("INVENTORY")
                    .status("FAILED")
                    .reason("Internal error: " + e.getMessage())
                    .productId(event.getProductId())
                    .build();

            intentResponseProducer.publishIntentResponse(response);
        }
    }
}
