package am.diploma.pvip.protocol.order.service;

import am.diploma.pvip.protocol.order.dto.*;
import am.diploma.pvip.protocol.order.entity.*;
import am.diploma.pvip.protocol.order.event.IntentRequestEvent;
import am.diploma.pvip.protocol.order.exception.NotFoundException;
import am.diploma.pvip.protocol.order.kafka.IntentRequestProducer;
import am.diploma.pvip.protocol.order.repository.OrderRepository;
import am.diploma.pvip.protocol.order.repository.ProtocolExecutionRepository;
import am.diploma.pvip.protocol.order.repository.TransactionIntentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProtocolExecutionRepository protocolExecutionRepository;
    private final TransactionIntentRepository transactionIntentRepository;
    private final PreValidationService preValidationService;
    private final IntentRequestProducer intentRequestProducer;

    @Transactional
    public PlaceOrderResponse placeOrder(PlaceOrderRequest request) {
        UUID transactionId = UUID.randomUUID();
        log.info("Placing order: transactionId={}, customerId={}", transactionId, request.customerId());

        PreValidationResult validation = preValidationService.validate(request.customerId(), request.items());

        if (!validation.passed()) {
            log.warn("Pre-validation failed for transaction={}: {}", transactionId, validation.reason());

            Order order = Order.builder()
                    .transactionId(transactionId)
                    .customerId(request.customerId())
                    .status(OrderStatus.FAILED)
                    .failureReason(validation.reason())
                    .build();
            order = orderRepository.save(order);

            ProtocolExecution execution = ProtocolExecution.builder()
                    .transactionId(transactionId)
                    .orderId(order.getId())
                    .status(ProtocolStatus.REJECTED)
                    .phase("PRE_VALIDATION")
                    .preValidationPassed(false)
                    .compensationsTriggered(0)
                    .kafkaMessagesSent(0)
                    .build();
            protocolExecutionRepository.save(execution);

            return new PlaceOrderResponse(order.getId(), transactionId, "FAILED", "PRE_VALIDATION",
                    0, 0, validation.reason(), null);
        }

        List<OrderItem> orderItems = validation.items().stream()
                .map(vi -> OrderItem.builder()
                        .productId(vi.productId())
                        .productName(vi.productName())
                        .quantity(vi.quantity())
                        .price(vi.price())
                        .build())
                .toList();

        Order order = Order.builder()
                .transactionId(transactionId)
                .customerId(request.customerId())
                .customerName(validation.customerName())
                .status(OrderStatus.PENDING)
                .totalAmount(validation.totalAmount())
                .items(new ArrayList<>(orderItems))
                .build();
        order = orderRepository.save(order);

        ProtocolExecution execution = ProtocolExecution.builder()
                .transactionId(transactionId)
                .orderId(order.getId())
                .status(ProtocolStatus.COLLECTING_INTENTS)
                .phase("COLLECTING_INTENTS")
                .preValidationPassed(true)
                .kafkaMessagesSent(2)
                .build();
        protocolExecutionRepository.save(execution);

        TransactionIntent inventoryIntent = TransactionIntent.builder()
                .transactionId(transactionId)
                .participantType("INVENTORY")
                .status("PENDING")
                .build();
        transactionIntentRepository.save(inventoryIntent);

        TransactionIntent paymentIntent = TransactionIntent.builder()
                .transactionId(transactionId)
                .participantType("PAYMENT")
                .status("PENDING")
                .build();
        transactionIntentRepository.save(paymentIntent);

        ValidatedItem firstItem = validation.items().getFirst();
        intentRequestProducer.publishIntentRequest(IntentRequestEvent.builder()
                .transactionId(transactionId)
                .participantType("INVENTORY")
                .productId(firstItem.productId())
                .quantity(firstItem.quantity())
                .build());

        intentRequestProducer.publishIntentRequest(IntentRequestEvent.builder()
                .transactionId(transactionId)
                .participantType("PAYMENT")
                .customerId(request.customerId())
                .amount(validation.totalAmount())
                .build());

        log.info("Order created: orderId={}, transactionId={}, status=PENDING", order.getId(), transactionId);
        return new PlaceOrderResponse(order.getId(), transactionId, "PENDING", "COLLECTING_INTENTS",
                2, 0, null, validation.totalAmount());
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("ORDER_NOT_FOUND", "Order not found: " + id));
        return toOrderResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toOrderResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProtocolExecutionResponse getProtocolExecution(UUID transactionId) {
        ProtocolExecution exec = protocolExecutionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new NotFoundException("EXECUTION_NOT_FOUND", "Protocol execution not found: " + transactionId));
        return toProtocolResponse(exec);
    }

    @Transactional(readOnly = true)
    public List<ProtocolExecutionResponse> getAllProtocolExecutions() {
        return protocolExecutionRepository.findAllByOrderByStartedAtDesc().stream()
                .map(this::toProtocolResponse)
                .toList();
    }

    private OrderResponse toOrderResponse(Order order) {
        List<OrderItemResponse> items = order.getItems() != null
                ? order.getItems().stream()
                    .map(i -> new OrderItemResponse(i.getProductId(), i.getProductName(), i.getQuantity(), i.getPrice()))
                    .toList()
                : List.of();
        return new OrderResponse(order.getId(), order.getTransactionId(), order.getCustomerId(),
                order.getCustomerName(), order.getStatus().name(), order.getTotalAmount(),
                order.getFailureReason(), order.getCreatedAt(), order.getUpdatedAt(), items);
    }

    private ProtocolExecutionResponse toProtocolResponse(ProtocolExecution exec) {
        return new ProtocolExecutionResponse(exec.getTransactionId(), exec.getOrderId(), exec.getStatus().name(),
                exec.getPhase(), exec.getKafkaMessagesSent(), exec.getCompensationsTriggered(),
                exec.getPreValidationPassed(), exec.getIntentResponsesReceived(), exec.getDecision(),
                exec.getDecisionReason(), exec.getStartedAt(), exec.getCompletedAt());
    }
}
