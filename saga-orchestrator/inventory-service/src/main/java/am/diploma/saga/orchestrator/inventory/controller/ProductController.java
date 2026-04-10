package am.diploma.saga.orchestrator.inventory.controller;

import am.diploma.saga.orchestrator.inventory.dto.ReleaseStockRequest;
import am.diploma.saga.orchestrator.inventory.dto.ReleaseStockResponse;
import am.diploma.saga.orchestrator.inventory.dto.ReserveStockRequest;
import am.diploma.saga.orchestrator.inventory.dto.ReserveStockResponse;
import am.diploma.saga.orchestrator.inventory.entity.Product;
import am.diploma.saga.orchestrator.inventory.exception.NotFoundException;
import am.diploma.saga.orchestrator.inventory.repository.ProductRepository;
import am.diploma.saga.orchestrator.inventory.service.InventoryService;
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

    @PostMapping("/release")
    public ReleaseStockResponse releaseStock(@RequestBody ReleaseStockRequest request) {
        return inventoryService.releaseStock(request);
    }
}
