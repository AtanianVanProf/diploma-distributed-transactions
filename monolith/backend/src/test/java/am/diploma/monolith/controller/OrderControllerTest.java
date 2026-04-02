package am.diploma.monolith.controller;

import am.diploma.monolith.dto.OrderItemResponse;
import am.diploma.monolith.dto.OrderResponse;
import am.diploma.monolith.exception.InsufficientBalanceException;
import am.diploma.monolith.exception.InsufficientStockException;
import am.diploma.monolith.exception.NotFoundException;
import am.diploma.monolith.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    private OrderResponse sampleOrderResponse() {
        return OrderResponse.builder()
                .orderId(1L)
                .customerId(1L)
                .status("COMPLETED")
                .totalAmount(new BigDecimal("2599.98"))
                .createdAt(LocalDateTime.of(2026, 4, 1, 12, 0))
                .items(List.of(
                        OrderItemResponse.builder()
                                .productId(1L)
                                .productName("Laptop Pro 15")
                                .quantity(2)
                                .priceAtPurchase(new BigDecimal("1299.99"))
                                .build()
                ))
                .build();
    }

    @Test
    @DisplayName("POST /api/orders returns 200 with OrderResponse on successful order")
    void placeOrder_success_returns200WithOrderResponse() throws Exception {
        when(orderService.placeOrder(any())).thenReturn(sampleOrderResponse());

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "customerId": 1,
                                    "items": [{"productId": 1, "quantity": 2}]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(1))
                .andExpect(jsonPath("$.customerId").value(1))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.totalAmount").value(2599.98))
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].productName").value("Laptop Pro 15"));
    }

    @Test
    @DisplayName("POST /api/orders returns 409 with INSUFFICIENT_STOCK when stock is insufficient")
    void placeOrder_insufficientStock_returns409() throws Exception {
        when(orderService.placeOrder(any()))
                .thenThrow(new InsufficientStockException(1L, "Laptop Pro 15", 5, 10));

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "customerId": 1,
                                    "items": [{"productId": 1, "quantity": 10}]
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("INSUFFICIENT_STOCK"));
    }

    @Test
    @DisplayName("POST /api/orders returns 409 with INSUFFICIENT_BALANCE when balance is insufficient")
    void placeOrder_insufficientBalance_returns409() throws Exception {
        when(orderService.placeOrder(any()))
                .thenThrow(new InsufficientBalanceException(2L, new BigDecimal("50.00"), new BigDecimal("2599.98")));

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "customerId": 2,
                                    "items": [{"productId": 1, "quantity": 2}]
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("INSUFFICIENT_BALANCE"));
    }

    @Test
    @DisplayName("POST /api/orders returns 422 with EMPTY_ORDER when items list is empty")
    void placeOrder_emptyItems_returns422() throws Exception {
        when(orderService.placeOrder(any()))
                .thenThrow(new IllegalArgumentException("EMPTY_ORDER"));

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "customerId": 1,
                                    "items": []
                                }
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("EMPTY_ORDER"));
    }

    @Test
    @DisplayName("GET /api/orders returns 200 with list of all orders")
    void getAllOrders_returns200WithOrderList() throws Exception {
        when(orderService.getAllOrders()).thenReturn(List.of(sampleOrderResponse()));

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].orderId").value(1))
                .andExpect(jsonPath("$[0].status").value("COMPLETED"));
    }

    @Test
    @DisplayName("GET /api/orders/{id} returns 200 with OrderResponse")
    void getOrderById_validId_returns200() throws Exception {
        when(orderService.getOrderById(1L)).thenReturn(sampleOrderResponse());

        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(1))
                .andExpect(jsonPath("$.totalAmount").value(2599.98))
                .andExpect(jsonPath("$.items[0].quantity").value(2));
    }

    @Test
    @DisplayName("GET /api/orders/{id} returns 404 with ORDER_NOT_FOUND for non-existent order")
    void getOrderById_invalidId_returns404() throws Exception {
        when(orderService.getOrderById(999L))
                .thenThrow(new NotFoundException("ORDER_NOT_FOUND", "Order with ID 999 not found"));

        mockMvc.perform(get("/api/orders/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("ORDER_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Order with ID 999 not found"));
    }
}
