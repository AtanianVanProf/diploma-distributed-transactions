package am.diploma.saga.orchestrator.order.service;

import am.diploma.saga.orchestrator.order.dto.CreateOrderRequest;
import am.diploma.saga.orchestrator.order.dto.OrderItemResponse;
import am.diploma.saga.orchestrator.order.dto.OrderResponse;
import am.diploma.saga.orchestrator.order.entity.Order;
import am.diploma.saga.orchestrator.order.entity.OrderItem;
import am.diploma.saga.orchestrator.order.entity.OrderStatus;
import am.diploma.saga.orchestrator.order.exception.NotFoundException;
import am.diploma.saga.orchestrator.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        Order order = Order.builder()
                .customerId(request.getCustomerId())
                .status(OrderStatus.valueOf(request.getStatus()))
                .totalAmount(request.getTotalAmount())
                .failureReason(request.getFailureReason())
                .build();

        List<OrderItem> items = request.getItems().stream()
                .map(itemReq -> OrderItem.builder()
                        .order(order)
                        .productId(itemReq.getProductId())
                        .productName(itemReq.getProductName())
                        .quantity(itemReq.getQuantity())
                        .priceAtPurchase(itemReq.getPrice())
                        .build())
                .toList();

        order.getItems().addAll(items);

        Order savedOrder = orderRepository.save(order);
        log.info("Created order with ID {} and status {}", savedOrder.getId(), savedOrder.getStatus());
        return toOrderResponse(savedOrder);
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
                .orElseThrow(() -> new NotFoundException("ORDER_NOT_FOUND", "Order with ID " + id + " not found"));
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
