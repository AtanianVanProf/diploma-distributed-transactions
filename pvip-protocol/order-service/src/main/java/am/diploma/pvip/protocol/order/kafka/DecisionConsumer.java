package am.diploma.pvip.protocol.order.kafka;

import am.diploma.pvip.protocol.order.entity.Order;
import am.diploma.pvip.protocol.order.entity.OrderStatus;
import am.diploma.pvip.protocol.order.entity.ProtocolExecution;
import am.diploma.pvip.protocol.order.entity.ProtocolStatus;
import am.diploma.pvip.protocol.order.entity.TransactionIntent;
import am.diploma.pvip.protocol.order.event.GlobalDecisionEvent;
import am.diploma.pvip.protocol.order.repository.OrderRepository;
import am.diploma.pvip.protocol.order.repository.ProtocolExecutionRepository;
import am.diploma.pvip.protocol.order.repository.TransactionIntentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class DecisionConsumer {

    private final OrderRepository orderRepository;
    private final ProtocolExecutionRepository protocolExecutionRepository;
    private final TransactionIntentRepository transactionIntentRepository;

    @KafkaListener(topics = "pvip.decisions", groupId = "order-service", containerFactory = "kafkaListenerContainerFactory")
    @Transactional
    public void consume(GlobalDecisionEvent event) {
        try {
            log.info("Received global decision: transactionId={}, decision={}", event.getTransactionId(), event.getDecision());

            if ("COMMIT".equals(event.getDecision())) {
                handleCommit(event);
            } else if ("REJECT".equals(event.getDecision())) {
                handleReject(event);
            }
        } catch (Exception e) {
            log.error("Error processing decision event: transactionId={}", event.getTransactionId(), e);
            throw e;
        }
    }

    private void handleCommit(GlobalDecisionEvent event) {
        Optional<Order> orderOpt = orderRepository.findByTransactionId(event.getTransactionId());
        if (orderOpt.isEmpty() || orderOpt.get().getStatus() != OrderStatus.PENDING) {
            return;
        }

        Order order = orderOpt.get();
        order.setStatus(OrderStatus.COMPLETED);
        orderRepository.save(order);

        protocolExecutionRepository.findByTransactionId(event.getTransactionId()).ifPresent(execution -> {
            execution.setStatus(ProtocolStatus.COMMITTED);
            execution.setPhase("COMMITTED");
            execution.setDecision("COMMIT");
            execution.setCompletedAt(LocalDateTime.now());
            protocolExecutionRepository.save(execution);
        });

        List<TransactionIntent> intents = transactionIntentRepository.findByTransactionId(event.getTransactionId());
        intents.forEach(intent -> intent.setStatus("COMMITTED"));
        transactionIntentRepository.saveAll(intents);
    }

    private void handleReject(GlobalDecisionEvent event) {
        Optional<Order> orderOpt = orderRepository.findByTransactionId(event.getTransactionId());
        if (orderOpt.isEmpty()) {
            return;
        }

        Order order = orderOpt.get();
        if (order.getStatus() == OrderStatus.COMPLETED || order.getStatus() == OrderStatus.FAILED) {
            return;
        }

        order.setStatus(OrderStatus.FAILED);
        order.setFailureReason(event.getReason());
        orderRepository.save(order);

        protocolExecutionRepository.findByTransactionId(event.getTransactionId()).ifPresent(execution -> {
            execution.setStatus(ProtocolStatus.REJECTED);
            execution.setPhase("REJECTED");
            execution.setDecision("REJECT");
            execution.setDecisionReason(event.getReason());
            execution.setCompletedAt(LocalDateTime.now());
            protocolExecutionRepository.save(execution);
        });

        List<TransactionIntent> intents = transactionIntentRepository.findByTransactionId(event.getTransactionId());
        intents.forEach(intent -> intent.setStatus("CANCELLED"));
        transactionIntentRepository.saveAll(intents);
    }
}
