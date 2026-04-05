package am.diploma.microservices.naive.inventory.controller;

import am.diploma.microservices.naive.inventory.dto.ReserveStockRequest;
import am.diploma.microservices.naive.inventory.dto.ReserveStockResponse;
import am.diploma.microservices.naive.inventory.entity.Product;
import am.diploma.microservices.naive.inventory.exception.NotFoundException;
import am.diploma.microservices.naive.inventory.repository.ProductRepository;
import am.diploma.microservices.naive.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductRepository productRepository;
    private final InventoryService inventoryService;

    @GetMapping
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @GetMapping("/{id}")
    public Product getProductById(@PathVariable Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("PRODUCT_NOT_FOUND",
                        "Product with ID " + id + " not found"));
    }

    @PostMapping("/reserve")
    public ReserveStockResponse reserveStock(@RequestBody ReserveStockRequest request) {
        return inventoryService.reserveStock(request);
    }
}
