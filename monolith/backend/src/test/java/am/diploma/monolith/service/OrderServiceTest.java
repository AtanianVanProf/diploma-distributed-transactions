package am.diploma.monolith.service;

import am.diploma.monolith.dto.CreateOrderRequest;
import am.diploma.monolith.dto.OrderItemRequest;
import am.diploma.monolith.dto.OrderResponse;
import am.diploma.monolith.entity.Customer;
import am.diploma.monolith.entity.Order;
import am.diploma.monolith.entity.OrderItem;
import am.diploma.monolith.entity.OrderStatus;
import am.diploma.monolith.entity.Product;
import am.diploma.monolith.exception.NotFoundException;
import am.diploma.monolith.repository.CustomerRepository;
import am.diploma.monolith.repository.OrderRepository;
import am.diploma.monolith.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private InventoryService inventoryService;

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private OrderService orderService;

    @Test
    @DisplayName("Successfully places an order and returns correct OrderResponse")
    void placeOrder_happyPath_returnsCompleteOrderResponse() {
        Customer customer = Customer.builder().id(1L).name("Alice").email("alice@example.com")
                .balance(new BigDecimal("10000.00")).build();
        Product laptop = Product.builder().id(1L).name("Laptop Pro 15").sku("LAP-001")
                .price(new BigDecimal("1299.99")).stock(10).build();

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(productRepository.findById(1L)).thenReturn(Optional.of(laptop));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(100L);
            return order;
        });

        CreateOrderRequest request = new CreateOrderRequest(1L, List.of(new OrderItemRequest(1L, 2)));

        OrderResponse response = orderService.placeOrder(request);

        assertEquals(100L, response.getOrderId());
        assertEquals(1L, response.getCustomerId());
        assertEquals("COMPLETED", response.getStatus());
        assertEquals(new BigDecimal("2599.98"), response.getTotalAmount());
        assertNotNull(response.getItems());
        assertEquals(1, response.getItems().size());
        assertEquals(1L, response.getItems().getFirst().getProductId());
        assertEquals("Laptop Pro 15", response.getItems().getFirst().getProductName());
        assertEquals(2, response.getItems().getFirst().getQuantity());
        assertEquals(new BigDecimal("1299.99"), response.getItems().getFirst().getPriceAtPurchase());
    }

    @Test
    @DisplayName("Throws IllegalArgumentException when items list is null")
    void placeOrder_nullItems_throwsIllegalArgumentException() {
        CreateOrderRequest request = new CreateOrderRequest(1L, null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> orderService.placeOrder(request));

        assertEquals("EMPTY_ORDER", ex.getMessage());
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Throws IllegalArgumentException when items list is empty")
    void placeOrder_emptyItems_throwsIllegalArgumentException() {
        CreateOrderRequest request = new CreateOrderRequest(1L, Collections.emptyList());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> orderService.placeOrder(request));

        assertEquals("EMPTY_ORDER", ex.getMessage());
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Throws NotFoundException when customer does not exist")
    void placeOrder_customerNotFound_throwsNotFoundException() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        CreateOrderRequest request = new CreateOrderRequest(99L, List.of(new OrderItemRequest(1L, 1)));

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> orderService.placeOrder(request));

        assertEquals("CUSTOMER_NOT_FOUND", ex.getErrorCode());
        verify(inventoryService, never()).reserveStock(any());
    }

    @Test
    @DisplayName("Stock reservation is called before payment processing")
    void placeOrder_verifyOrdering_stockReservedBeforePayment() {
        Customer customer = Customer.builder().id(1L).name("Alice").email("alice@example.com")
                .balance(new BigDecimal("10000.00")).build();
        Product mouse = Product.builder().id(2L).name("Wireless Mouse").sku("MOU-001")
                .price(new BigDecimal("29.99")).stock(20).build();

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(productRepository.findById(2L)).thenReturn(Optional.of(mouse));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(101L);
            return order;
        });

        List<OrderItemRequest> items = List.of(new OrderItemRequest(2L, 1));
        CreateOrderRequest request = new CreateOrderRequest(1L, items);

        orderService.placeOrder(request);

        InOrder inOrder = inOrder(inventoryService, paymentService);
        inOrder.verify(inventoryService).reserveStock(items);
        inOrder.verify(paymentService).processPayment(eq(1L), any(BigDecimal.class));
    }

    @Test
    @DisplayName("Calculates correct totalAmount for multi-item order")
    void placeOrder_multipleItems_calculatesCorrectTotal() {
        Customer customer = Customer.builder().id(1L).name("Alice").email("alice@example.com")
                .balance(new BigDecimal("10000.00")).build();
        Product laptop = Product.builder().id(1L).name("Laptop Pro 15").sku("LAP-001")
                .price(new BigDecimal("1299.99")).stock(10).build();
        Product mouse = Product.builder().id(2L).name("Wireless Mouse").sku("MOU-001")
                .price(new BigDecimal("29.99")).stock(20).build();

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(productRepository.findById(1L)).thenReturn(Optional.of(laptop));
        when(productRepository.findById(2L)).thenReturn(Optional.of(mouse));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(102L);
            return order;
        });

        CreateOrderRequest request = new CreateOrderRequest(1L, List.of(
                new OrderItemRequest(1L, 2),
                new OrderItemRequest(2L, 3)
        ));

        OrderResponse response = orderService.placeOrder(request);

        assertEquals(new BigDecimal("2689.95"), response.getTotalAmount());
        assertEquals(2, response.getItems().size());
    }

    @Test
    @DisplayName("Order status is set to COMPLETED on successful order")
    void placeOrder_success_statusIsCompleted() {
        Customer customer = Customer.builder().id(1L).name("Alice").email("alice@example.com")
                .balance(new BigDecimal("10000.00")).build();
        Product product = Product.builder().id(1L).name("USB-C Hub").sku("USB-001")
                .price(new BigDecimal("49.99")).stock(5).build();

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(103L);
            return order;
        });

        CreateOrderRequest request = new CreateOrderRequest(1L, List.of(new OrderItemRequest(1L, 1)));

        OrderResponse response = orderService.placeOrder(request);

        assertEquals("COMPLETED", response.getStatus());
    }

    private Order buildOrderWithItems(Long orderId, Customer customer, Product product, int quantity) {
        Order order = Order.builder()
                .id(orderId)
                .customer(customer)
                .status(OrderStatus.COMPLETED)
                .totalAmount(product.getPrice().multiply(BigDecimal.valueOf(quantity)))
                .createdAt(LocalDateTime.of(2026, 4, 1, 12, 0))
                .items(new ArrayList<>())
                .build();
        OrderItem item = OrderItem.builder()
                .id(orderId * 10)
                .order(order)
                .product(product)
                .quantity(quantity)
                .priceAtPurchase(product.getPrice())
                .build();
        order.getItems().add(item);
        return order;
    }

    @Test
    @DisplayName("getAllOrders returns mapped OrderResponse list")
    void getAllOrders_returnsAllMappedResponses() {
        Customer customer = Customer.builder().id(1L).name("Alice").email("alice@example.com")
                .balance(new BigDecimal("10000.00")).build();
        Product laptop = Product.builder().id(1L).name("Laptop Pro 15").sku("LAP-001")
                .price(new BigDecimal("1299.99")).stock(10).build();
        Product mouse = Product.builder().id(2L).name("Wireless Mouse").sku("MOU-001")
                .price(new BigDecimal("29.99")).stock(20).build();

        Order order1 = buildOrderWithItems(1L, customer, laptop, 2);
        Order order2 = buildOrderWithItems(2L, customer, mouse, 1);

        when(orderRepository.findAllWithItems()).thenReturn(List.of(order1, order2));

        List<OrderResponse> responses = orderService.getAllOrders();

        assertEquals(2, responses.size());
        assertEquals(1L, responses.getFirst().getOrderId());
        assertEquals(new BigDecimal("2599.98"), responses.getFirst().getTotalAmount());
        assertEquals(2L, responses.get(1).getOrderId());
        assertEquals(new BigDecimal("29.99"), responses.get(1).getTotalAmount());
    }

    @Test
    @DisplayName("getAllOrders returns empty list when no orders exist")
    void getAllOrders_noOrders_returnsEmptyList() {
        when(orderRepository.findAllWithItems()).thenReturn(Collections.emptyList());

        List<OrderResponse> responses = orderService.getAllOrders();

        assertEquals(0, responses.size());
    }

    @Test
    @DisplayName("getOrderById returns correct OrderResponse with items")
    void getOrderById_existingOrder_returnsCorrectResponse() {
        Customer customer = Customer.builder().id(1L).name("Alice").email("alice@example.com")
                .balance(new BigDecimal("10000.00")).build();
        Product laptop = Product.builder().id(1L).name("Laptop Pro 15").sku("LAP-001")
                .price(new BigDecimal("1299.99")).stock(10).build();

        Order order = buildOrderWithItems(1L, customer, laptop, 2);
        when(orderRepository.findByIdWithItems(1L)).thenReturn(Optional.of(order));

        OrderResponse response = orderService.getOrderById(1L);

        assertEquals(1L, response.getOrderId());
        assertEquals(1L, response.getCustomerId());
        assertEquals("COMPLETED", response.getStatus());
        assertEquals(new BigDecimal("2599.98"), response.getTotalAmount());
        assertEquals(1, response.getItems().size());
        assertEquals("Laptop Pro 15", response.getItems().getFirst().getProductName());
    }

    @Test
    @DisplayName("getOrderById throws NotFoundException for non-existent order")
    void getOrderById_orderNotFound_throwsNotFoundException() {
        when(orderRepository.findByIdWithItems(99L)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> orderService.getOrderById(99L));

        assertEquals("ORDER_NOT_FOUND", ex.getErrorCode());
        assertEquals("Order with ID 99 not found", ex.getMessage());
    }
}
