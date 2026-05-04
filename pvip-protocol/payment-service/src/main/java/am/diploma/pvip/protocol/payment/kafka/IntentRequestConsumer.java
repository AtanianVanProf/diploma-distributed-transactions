package am.diploma.pvip.protocol.payment.kafka;

import am.diploma.pvip.protocol.payment.entity.Customer;
import am.diploma.pvip.protocol.payment.entity.TransactionIntent;
import am.diploma.pvip.protocol.payment.event.IntentRequestEvent;
import am.diploma.pvip.protocol.payment.event.IntentResponseEvent;
import am.diploma.pvip.protocol.payment.service.CustomerService;
import am.diploma.pvip.protocol.payment.service.IntentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class IntentRequestConsumer {

    private final IntentService intentService;
    private final CustomerService customerService;
    private final IntentResponseProducer intentResponseProducer;

    @KafkaListener(topics = "pvip.intent-requests", groupId = "payment-service", containerFactory = "kafkaListenerContainerFactory")
    public void consume(IntentRequestEvent event) {
        if (!"PAYMENT".equals(event.getParticipantType())) {
            log.debug("Ignoring intent request for participantType={}", event.getParticipantType());
            return;
        }

        log.info("Received intent request for transaction={}, customer={}, amount={}",
                event.getTransactionId(), event.getCustomerId(), event.getAmount());

        try {
            TransactionIntent intent = intentService.registerIntent(
                    event.getTransactionId(), event.getCustomerId(), event.getAmount());

            Customer customer = customerService.getCustomer(intent.getCustomerId());

            IntentResponseEvent response = IntentResponseEvent.builder()
                    .transactionId(intent.getTransactionId())
                    .participantType("PAYMENT")
                    .status(intent.getStatus().name())
                    .reason(intent.getReason())
                    .customerId(customer.getId())
                    .customerName(customer.getName())
                    .availableBalance(customer.getBalance())
                    .build();

            intentResponseProducer.publishIntentResponse(response);
        } catch (Exception e) {
            log.error("Failed to process intent request for transaction={}: {}", event.getTransactionId(), e.getMessage());

            IntentResponseEvent response = IntentResponseEvent.builder()
                    .transactionId(event.getTransactionId())
                    .participantType("PAYMENT")
                    .status("FAILED")
                    .reason("Internal error: " + e.getMessage())
                    .customerId(event.getCustomerId())
                    .build();

            intentResponseProducer.publishIntentResponse(response);
        }
    }
}
