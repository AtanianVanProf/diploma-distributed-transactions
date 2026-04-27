package am.diploma.microservices.naive.order.service;

import am.diploma.microservices.naive.order.client.InventoryClient;
import am.diploma.microservices.naive.order.client.PaymentClient;
import am.diploma.microservices.naive.order.dto.CreateOrderRequest;
import am.diploma.microservices.naive.order.dto.OrderItemResponse;
import am.diploma.microservices.naive.order.dto.OrderResponse;
import am.diploma.microservices.naive.order.dto.ReserveStockResponse;
import am.diploma.microservices.naive.order.dto.ReservedItemResponse;
import am.diploma.microservices.naive.order.entity.Order;
import am.diploma.microservices.naive.order.entity.OrderItem;
import am.diploma.microservices.naive.order.entity.OrderStatus;
import am.diploma.microservices.naive.order.exception.NotFoundException;
import am.diploma.microservices.naive.order.exception.ServiceCallException;
import am.diploma.microservices.naive.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final InventoryClient inventoryClient;
    private final PaymentClient paymentClient;

    @Transactional
    public OrderResponse placeOrder(CreateOrderRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new ServiceCallException("ORDER", "EMPTY_ORDER", "Order must contain at least one item");
        }

        ReserveStockResponse stockResponse = inventoryClient.reserveStock(request.getItems());

        BigDecimal totalAmount = stockResponse.getItems().stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getReservedQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        try {
            paymentClient.charge(request.getCustomerId(), totalAmount);
        } catch (ServiceCallException ex) {
            log.warn("Payment failed for order. Inventory was already committed. Stock for products {} has been permanently decremented. No compensation mechanism is available.",
                    stockResponse.getItems().stream()
                            .map(item -> item.getProductName() + " (qty: " + item.getReservedQuantity() + ")")
                            .toList());

            Order failedOrder = Order.builder()
                    .customerId(request.getCustomerId())
                    .status(OrderStatus.FAILED)
                    .totalAmount(totalAmount)
                    .failureReason(ex.getErrorCode() + ": " + ex.getErrorMessage())
                    .build();

            List<OrderItem> failedItems = buildOrderItems(stockResponse, failedOrder);
            failedOrder.getItems().addAll(failedItems);

            Order savedFailedOrder = orderRepository.save(failedOrder);
            return toOrderResponse(savedFailedOrder);
        }

        Order order = Order.builder()
                .customerId(request.getCustomerId())
                .status(OrderStatus.COMPLETED)
                .totalAmount(totalAmount)
                .build();

        List<OrderItem> items = buildOrderItems(stockResponse, order);
        order.getItems().addAll(items);

        Order savedOrder = orderRepository.save(order);
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
                .orElseThrow(() -> new NotFoundException("Order with ID " + id + " not found", "ORDER_NOT_FOUND"));
        return toOrderResponse(order);
    }

    private List<OrderItem> buildOrderItems(ReserveStockResponse stockResponse, Order order) {
        return stockResponse.getItems().stream()
                .map(item -> OrderItem.builder()
                        .order(order)
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .quantity(item.getReservedQuantity())
                        .priceAtPurchase(item.getPrice())
                        .build())
                .toList();
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
