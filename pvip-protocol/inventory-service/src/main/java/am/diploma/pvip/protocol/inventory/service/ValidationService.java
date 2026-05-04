package am.diploma.pvip.protocol.inventory.service;

import am.diploma.pvip.protocol.inventory.dto.ValidationResponse;
import am.diploma.pvip.protocol.inventory.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ValidationService {

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public ValidationResponse validateStock(Long productId, Integer quantity) {
        return productRepository.findById(productId)
                .map(product -> {
                    if (product.getStock() < quantity) {
                        return new ValidationResponse(
                                false,
                                product.getId(),
                                product.getName(),
                                product.getStock(),
                                quantity,
                                product.getPrice(),
                                String.format("Insufficient stock: available %d, requested %d",
                                        product.getStock(), quantity)
                        );
                    }
                    return new ValidationResponse(
                            true,
                            product.getId(),
                            product.getName(),
                            product.getStock(),
                            quantity,
                            product.getPrice(),
                            null
                    );
                })
                .orElse(new ValidationResponse(
                        false,
                        productId,
                        null,
                        null,
                        quantity,
                        null,
                        "Product not found"
                ));
    }
}
