package am.diploma.saga.choreography.order.service;

import am.diploma.saga.choreography.order.dto.*;
import am.diploma.saga.choreography.order.entity.*;
import am.diploma.saga.choreography.order.event.OrderPlacedEvent;
import am.diploma.saga.choreography.order.exception.NotFoundException;
import am.diploma.saga.choreography.order.kafka.OrderEventProducer;
import am.diploma.saga.choreography.order.repository.OrderRepository;
import am.diploma.saga.choreography.order.repository.SagaExecutionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final SagaExecutionRepository sagaExecutionRepository;
    private final ObjectMapper objectMapper;
    private final OrderEventProducer orderEventProducer;

    @Transactional
    public PlaceOrderResponse placeOrder(PlaceOrderRequest request) {
        Order order = Order.builder()
                .customerId(request.getCustomerId())
                .status(OrderStatus.PENDING)
                .build();

        List<OrderItem> items = request.getItems().stream()
                .map(itemReq -> OrderItem.builder()
                        .order(order)
                        .productId(itemReq.getProductId())
                        .quantity(itemReq.getQuantity())
                        .build())
                .toList();

        order.getItems().addAll(items);
        Order savedOrder = orderRepository.save(order);

        String requestPayload;
        try {
            requestPayload = objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize order request payload: {}", e.getMessage());
            requestPayload = "{}";
        }

        SagaExecution saga = SagaExecution.builder()
                .orderId(savedOrder.getId())
                .status(SagaStatus.STARTED)
                .requestPayload(requestPayload)
                .build();

        SagaStep reserveStock = SagaStep.builder()
                .sagaExecution(saga)
                .stepName("RESERVE_STOCK")
                .stepOrder(1)
                .status(StepStatus.PENDING)
                .build();

        SagaStep chargePayment = SagaStep.builder()
                .sagaExecution(saga)
                .stepName("CHARGE_PAYMENT")
                .stepOrder(2)
                .status(StepStatus.PENDING)
                .build();

        SagaStep createOrder = SagaStep.builder()
                .sagaExecution(saga)
                .stepName("CREATE_ORDER")
                .stepOrder(3)
                .status(StepStatus.PENDING)
                .build();

        saga.getSteps().addAll(List.of(reserveStock, chargePayment, createOrder));
        SagaExecution savedSaga = sagaExecutionRepository.save(saga);

        log.info("Order {} created with saga {}", savedOrder.getId(), savedSaga.getId());

        OrderPlacedEvent event = OrderPlacedEvent.builder()
                .sagaId(savedSaga.getId())
                .orderId(savedOrder.getId())
                .customerId(savedOrder.getCustomerId())
                .items(request.getItems().stream()
                        .map(item -> OrderPlacedEvent.OrderItem.builder()
                                .productId(item.getProductId())
                                .quantity(item.getQuantity())
                                .build())
                        .toList())
                .build();

        orderEventProducer.publishOrderPlaced(event);

        return PlaceOrderResponse.builder()
                .orderId(savedOrder.getId())
                .sagaId(savedSaga.getId())
                .build();
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAllWithItems().stream()
                .map(this::toOrderResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findByIdWithItems(id)
                .orElseThrow(() -> new NotFoundException("ORDER_NOT_FOUND",
                        "Order with ID " + id + " not found"));
        return toOrderResponse(order);
    }

    private OrderResponse toOrderResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(item -> OrderItemResponse.builder()
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .quantity(item.getQuantity())
                        .priceAtPurchase(item.getPriceAtPurchase())
                        .build())
                .toList();

        return OrderResponse.builder()
                .orderId(order.getId())
                .customerId(order.getCustomerId())
                .status(order.getStatus().name())
                .totalAmount(order.getTotalAmount())
                .failureReason(order.getFailureReason())
                .items(itemResponses)
                .createdAt(order.getCreatedAt())
                .build();
    }
}
