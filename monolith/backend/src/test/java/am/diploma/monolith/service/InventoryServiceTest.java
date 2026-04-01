package am.diploma.monolith.service;

import am.diploma.monolith.dto.OrderItemRequest;
import am.diploma.monolith.entity.Product;
import am.diploma.monolith.exception.InsufficientStockException;
import am.diploma.monolith.exception.NotFoundException;
import am.diploma.monolith.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private InventoryService inventoryService;

    @Test
    @DisplayName("Successfully reserves stock for a single item")
    void reserveStock_singleItem_decrementsStock() {
        Product product = Product.builder()
                .id(1L)
                .name("Laptop Pro 15")
                .sku("LAP-001")
                .price(new BigDecimal("999.99"))
                .stock(10)
                .build();
        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(product));

        inventoryService.reserveStock(List.of(new OrderItemRequest(1L, 3)));

        assertEquals(7, product.getStock());
    }

    @Test
    @DisplayName("Throws NotFoundException when product does not exist")
    void reserveStock_productNotFound_throwsNotFoundException() {
        when(productRepository.findByIdForUpdate(99L)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> inventoryService.reserveStock(List.of(new OrderItemRequest(99L, 1))));

        assertEquals("PRODUCT_NOT_FOUND", ex.getErrorCode());
        assertEquals("Product with ID 99 not found", ex.getMessage());
    }

    @Test
    @DisplayName("Throws InsufficientStockException when stock is less than requested")
    void reserveStock_insufficientStock_throwsInsufficientStockException() {
        Product product = Product.builder()
                .id(5L)
                .name("Wireless Mouse")
                .sku("MOU-001")
                .price(new BigDecimal("29.99"))
                .stock(1)
                .build();
        when(productRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(product));

        InsufficientStockException ex = assertThrows(InsufficientStockException.class,
                () -> inventoryService.reserveStock(List.of(new OrderItemRequest(5L, 5))));

        assertEquals(5L, ex.getProductId());
        assertEquals("Wireless Mouse", ex.getProductName());
        assertEquals(1, ex.getAvailable());
        assertEquals(5, ex.getRequested());
    }

    @Test
    @DisplayName("Successfully reserves stock for multiple items")
    void reserveStock_multipleItems_decrementsAllStocks() {
        Product laptop = Product.builder()
                .id(1L)
                .name("Laptop Pro 15")
                .sku("LAP-001")
                .price(new BigDecimal("999.99"))
                .stock(10)
                .build();
        Product mouse = Product.builder()
                .id(2L)
                .name("Wireless Mouse")
                .sku("MOU-001")
                .price(new BigDecimal("29.99"))
                .stock(20)
                .build();
        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(laptop));
        when(productRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(mouse));

        inventoryService.reserveStock(List.of(
                new OrderItemRequest(1L, 3),
                new OrderItemRequest(2L, 5)
        ));

        assertEquals(7, laptop.getStock());
        assertEquals(15, mouse.getStock());
    }
}
