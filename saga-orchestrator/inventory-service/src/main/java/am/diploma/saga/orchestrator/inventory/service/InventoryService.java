package am.diploma.saga.orchestrator.inventory.service;

import am.diploma.saga.orchestrator.inventory.dto.ReleaseStockRequest;
import am.diploma.saga.orchestrator.inventory.dto.ReleaseStockResponse;
import am.diploma.saga.orchestrator.inventory.dto.ReleasedItemResponse;
import am.diploma.saga.orchestrator.inventory.dto.ReserveStockRequest;
import am.diploma.saga.orchestrator.inventory.dto.ReserveStockResponse;
import am.diploma.saga.orchestrator.inventory.dto.ReservedItemResponse;
import am.diploma.saga.orchestrator.inventory.entity.Product;
import am.diploma.saga.orchestrator.inventory.exception.InsufficientStockException;
import am.diploma.saga.orchestrator.inventory.exception.NotFoundException;
import am.diploma.saga.orchestrator.inventory.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final ProductRepository productRepository;

    @Transactional
    public ReserveStockResponse reserveStock(ReserveStockRequest request) {
        List<ReservedItemResponse> reservedItems = new ArrayList<>();

        for (var item : request.getItems()) {
            Product product = productRepository.findByIdForUpdate(item.getProductId())
                    .orElseThrow(() -> new NotFoundException("PRODUCT_NOT_FOUND",
                            "Product with ID " + item.getProductId() + " not found"));

            if (product.getStock() < item.getQuantity()) {
                throw new InsufficientStockException(
                        product.getId(), product.getName(), product.getStock(), item.getQuantity());
            }

            product.setStock(product.getStock() - item.getQuantity());

            reservedItems.add(ReservedItemResponse.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .price(product.getPrice())
                    .reservedQuantity(item.getQuantity())
                    .build());
        }

        return ReserveStockResponse.builder()
                .reserved(true)
                .items(reservedItems)
                .build();
    }

    @Transactional
    public ReleaseStockResponse releaseStock(ReleaseStockRequest request) {
        List<ReleasedItemResponse> releasedItems = new ArrayList<>();

        for (var item : request.getItems()) {
            Product product = productRepository.findByIdForUpdate(item.getProductId())
                    .orElseThrow(() -> new NotFoundException("PRODUCT_NOT_FOUND",
                            "Product with ID " + item.getProductId() + " not found"));

            product.setStock(product.getStock() + item.getQuantity());

            releasedItems.add(ReleasedItemResponse.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .releasedQuantity(item.getQuantity())
                    .build());
        }

        return ReleaseStockResponse.builder()
                .released(true)
                .items(releasedItems)
                .build();
    }
}
