package am.diploma.monolith.service;

import am.diploma.monolith.dto.OrderItemRequest;
import am.diploma.monolith.entity.Product;
import am.diploma.monolith.exception.InsufficientStockException;
import am.diploma.monolith.exception.NotFoundException;
import am.diploma.monolith.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final ProductRepository productRepository;

    public void reserveStock(List<OrderItemRequest> items) {
        for (OrderItemRequest item : items) {
            Product product = productRepository.findByIdForUpdate(item.getProductId())
                    .orElseThrow(() -> new NotFoundException("PRODUCT_NOT_FOUND",
                            "Product with ID " + item.getProductId() + " not found"));

            if (product.getStock() < item.getQuantity()) {
                throw new InsufficientStockException(
                        product.getId(), product.getName(), product.getStock(), item.getQuantity());
            }

            product.setStock(product.getStock() - item.getQuantity());
        }
    }
}
