package am.diploma.monolith.controller;

import am.diploma.monolith.entity.Product;
import am.diploma.monolith.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductRepository productRepository;

    @Test
    @DisplayName("GET /api/products returns 200 with list of all products including stock")
    void getAllProducts_returns200WithProductList() throws Exception {
        Product laptop = Product.builder().id(1L).name("Laptop Pro 15").sku("LAP-PRO-15")
                .price(new BigDecimal("1299.99")).stock(10).build();
        Product mouse = Product.builder().id(2L).name("Wireless Mouse").sku("WRL-MOUSE")
                .price(new BigDecimal("29.99")).stock(3).build();

        when(productRepository.findAll()).thenReturn(List.of(laptop, mouse));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Laptop Pro 15"))
                .andExpect(jsonPath("$[0].sku").value("LAP-PRO-15"))
                .andExpect(jsonPath("$[0].price").value(1299.99))
                .andExpect(jsonPath("$[0].stock").value(10))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].stock").value(3));
    }

    @Test
    @DisplayName("GET /api/products/{id} returns 200 with product data including stock")
    void getProductById_validId_returns200WithProduct() throws Exception {
        Product laptop = Product.builder().id(1L).name("Laptop Pro 15").sku("LAP-PRO-15")
                .price(new BigDecimal("1299.99")).stock(10).build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(laptop));

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Laptop Pro 15"))
                .andExpect(jsonPath("$.sku").value("LAP-PRO-15"))
                .andExpect(jsonPath("$.price").value(1299.99))
                .andExpect(jsonPath("$.stock").value(10));
    }

    @Test
    @DisplayName("GET /api/products/{id} returns 404 with PRODUCT_NOT_FOUND for non-existent ID")
    void getProductById_invalidId_returns404() throws Exception {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/products/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("PRODUCT_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Product with ID 999 not found"));
    }
}
