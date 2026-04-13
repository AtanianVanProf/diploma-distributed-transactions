package am.diploma.saga.choreography.inventory.service;

import am.diploma.saga.choreography.inventory.entity.Product;
import am.diploma.saga.choreography.inventory.exception.InsufficientStockException;
import am.diploma.saga.choreography.inventory.exception.NotFoundException;
import am.diploma.saga.choreography.inventory.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final ProductRepository productRepository;

    public record StockItemData(Long productId, Integer quantity) {}

    public record ReservedItem(Long productId, String productName, Integer quantity, BigDecimal price) {}

    public record ReservationResult(List<ReservedItem> items, BigDecimal totalAmount) {}

    @Transactional
    public ReservationResult reserveStock(List<StockItemData> items) {
        List<ReservedItem> reservedItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (var item : items) {
            Product product = productRepository.findByIdForUpdate(item.productId())
                    .orElseThrow(() -> new NotFoundException("PRODUCT_NOT_FOUND",
                            "Product with ID " + item.productId() + " not found"));

            if (product.getStock() < item.quantity()) {
                throw new InsufficientStockException(
                        product.getId(), product.getName(), product.getStock(), item.quantity());
            }

            product.setStock(product.getStock() - item.quantity());

            reservedItems.add(new ReservedItem(
                    product.getId(),
                    product.getName(),
                    item.quantity(),
                    product.getPrice()));

            totalAmount = totalAmount.add(product.getPrice().multiply(BigDecimal.valueOf(item.quantity())));
        }

        return new ReservationResult(reservedItems, totalAmount);
    }

    @Transactional
    public void releaseStock(List<StockItemData> items) {
        for (var item : items) {
            Product product = productRepository.findByIdForUpdate(item.productId())
                    .orElseThrow(() -> new NotFoundException("PRODUCT_NOT_FOUND",
                            "Product with ID " + item.productId() + " not found"));

            product.setStock(product.getStock() + item.quantity());
        }
    }
}
