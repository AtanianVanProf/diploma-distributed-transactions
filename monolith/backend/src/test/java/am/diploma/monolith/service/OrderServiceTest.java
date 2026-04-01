package am.diploma.monolith.service;

import am.diploma.monolith.dto.CreateOrderRequest;
import am.diploma.monolith.dto.OrderItemRequest;
import am.diploma.monolith.dto.OrderResponse;
import am.diploma.monolith.entity.Customer;
import am.diploma.monolith.entity.Order;
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

        // 1299.99 * 2 + 29.99 * 3 = 2599.98 + 89.97 = 2689.95
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
}
