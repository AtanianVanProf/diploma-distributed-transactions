package am.diploma.monolith.exception;

import am.diploma.monolith.dto.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("InsufficientStockException returns 409 Conflict with product details")
    void handleInsufficientStock_returns409WithDetails() {
        InsufficientStockException ex = new InsufficientStockException(1L, "Laptop Pro 15", 5, 10);

        ResponseEntity<ErrorResponse> response = handler.handleInsufficientStock(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals("INSUFFICIENT_STOCK", body.getError());
        assertEquals(ex.getMessage(), body.getMessage());
        assertNotNull(body.getDetails());
        assertEquals(1L, body.getDetails().get("productId"));
        assertEquals(5, body.getDetails().get("available"));
        assertEquals(10, body.getDetails().get("requested"));
    }

    @Test
    @DisplayName("InsufficientBalanceException returns 409 Conflict with customer details")
    void handleInsufficientBalance_returns409WithDetails() {
        InsufficientBalanceException ex = new InsufficientBalanceException(
                2L, new BigDecimal("50.00"), new BigDecimal("1299.99"));

        ResponseEntity<ErrorResponse> response = handler.handleInsufficientBalance(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals("INSUFFICIENT_BALANCE", body.getError());
        assertEquals(ex.getMessage(), body.getMessage());
        assertNotNull(body.getDetails());
        assertEquals(2L, body.getDetails().get("customerId"));
        assertEquals(new BigDecimal("50.00"), body.getDetails().get("available"));
        assertEquals(new BigDecimal("1299.99"), body.getDetails().get("requested"));
    }

    @Test
    @DisplayName("NotFoundException returns 404 Not Found with CUSTOMER_NOT_FOUND error code")
    void handleNotFound_returns404WithErrorCode() {
        NotFoundException ex = new NotFoundException("CUSTOMER_NOT_FOUND", "Customer with ID 999 not found");

        ResponseEntity<ErrorResponse> response = handler.handleNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals("CUSTOMER_NOT_FOUND", body.getError());
        assertEquals("Customer with ID 999 not found", body.getMessage());
        assertNull(body.getDetails());
    }

    @Test
    @DisplayName("NotFoundException returns 404 Not Found with PRODUCT_NOT_FOUND error code")
    void handleNotFound_returnsProductNotFound() {
        NotFoundException ex = new NotFoundException("PRODUCT_NOT_FOUND", "Product with ID 42 not found");

        ResponseEntity<ErrorResponse> response = handler.handleNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals("PRODUCT_NOT_FOUND", body.getError());
    }

    @Test
    @DisplayName("DataIntegrityViolationException returns 409 Conflict with generic constraint message")
    void handleDataIntegrityViolation_returns409WithGenericMessage() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException("constraint violation");

        ResponseEntity<ErrorResponse> response = handler.handleDataIntegrityViolation(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals("CONSTRAINT_VIOLATION", body.getError());
        assertEquals("A database constraint was violated", body.getMessage());
        assertNull(body.getDetails());
    }
}
