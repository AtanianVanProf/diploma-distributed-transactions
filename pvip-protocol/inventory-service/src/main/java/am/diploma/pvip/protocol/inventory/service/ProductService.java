package am.diploma.pvip.protocol.inventory.service;

import am.diploma.pvip.protocol.inventory.entity.Product;
import am.diploma.pvip.protocol.inventory.exception.NotFoundException;
import am.diploma.pvip.protocol.inventory.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Product getProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "PRODUCT_NOT_FOUND",
                        "Product with id " + id + " not found"
                ));
    }
}
