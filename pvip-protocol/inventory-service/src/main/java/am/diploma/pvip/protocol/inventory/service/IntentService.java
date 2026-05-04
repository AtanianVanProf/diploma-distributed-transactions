package am.diploma.pvip.protocol.inventory.service;

import am.diploma.pvip.protocol.inventory.entity.IntentStatus;
import am.diploma.pvip.protocol.inventory.entity.Product;
import am.diploma.pvip.protocol.inventory.entity.TransactionIntent;
import am.diploma.pvip.protocol.inventory.exception.NotFoundException;
import am.diploma.pvip.protocol.inventory.repository.ProductRepository;
import am.diploma.pvip.protocol.inventory.repository.TransactionIntentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class IntentService {

    private final TransactionIntentRepository transactionIntentRepository;
    private final ProductRepository productRepository;

    @Transactional
    public TransactionIntent registerIntent(UUID transactionId, Long productId, Integer quantity) {
        log.info("Registering intent for transaction={}, product={}, quantity={}", transactionId, productId, quantity);

        var existing = transactionIntentRepository.findByTransactionIdAndProductId(transactionId, productId);
        if (existing.isPresent()) {
            log.info("Intent already exists for transaction={}, product={}", transactionId, productId);
            return existing.get();
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("PRODUCT_NOT_FOUND", "Product not found: " + productId));

        TransactionIntent intent;
        if (product.getStock() >= quantity) {
            intent = TransactionIntent.builder()
                    .transactionId(transactionId)
                    .productId(productId)
                    .quantity(quantity)
                    .status(IntentStatus.READY)
                    .build();
            log.info("Intent READY for transaction={}, product={}", transactionId, productId);
        } else {
            String reason = String.format("Insufficient stock: requested=%d, available=%d", quantity, product.getStock());
            intent = TransactionIntent.builder()
                    .transactionId(transactionId)
                    .productId(productId)
                    .quantity(quantity)
                    .status(IntentStatus.FAILED)
                    .reason(reason)
                    .build();
            log.warn("Intent FAILED for transaction={}, product={}: {}", transactionId, productId, reason);
        }

        return transactionIntentRepository.save(intent);
    }

    @Transactional
    public void finalizeIntent(UUID transactionId) {
        log.info("Finalizing intents for transaction={}", transactionId);

        List<TransactionIntent> intents = transactionIntentRepository.findByTransactionId(transactionId);
        if (intents.isEmpty()) {
            log.warn("No intents found for transaction={}", transactionId);
            return;
        }

        for (TransactionIntent intent : intents) {
            if (intent.getStatus() == IntentStatus.COMMITTED || intent.getStatus() == IntentStatus.CANCELLED) {
                log.info("Skipping already {} intent id={}", intent.getStatus(), intent.getId());
                continue;
            }

            if (intent.getStatus() == IntentStatus.READY) {
                Product product = productRepository.findByIdForUpdate(intent.getProductId())
                        .orElseThrow(() -> new NotFoundException("PRODUCT_NOT_FOUND", "Product not found: " + intent.getProductId()));

                if (product.getStock() < intent.getQuantity()) {
                    intent.setStatus(IntentStatus.FAILED);
                    intent.setReason("Insufficient stock at commit time");
                    transactionIntentRepository.save(intent);
                    log.warn("Intent id={} failed at commit: stock={}, requested={}", intent.getId(), product.getStock(), intent.getQuantity());
                    continue;
                }

                product.setStock(product.getStock() - intent.getQuantity());
                productRepository.save(product);

                intent.setStatus(IntentStatus.COMMITTED);
                transactionIntentRepository.save(intent);
                log.info("Intent id={} committed, deducted {} from product={}", intent.getId(), intent.getQuantity(), intent.getProductId());
            }
        }
    }

    @Transactional
    public void cancelIntent(UUID transactionId) {
        log.info("Cancelling intents for transaction={}", transactionId);

        List<TransactionIntent> intents = transactionIntentRepository.findByTransactionId(transactionId);
        if (intents.isEmpty()) {
            log.warn("No intents found for transaction={}", transactionId);
            return;
        }

        for (TransactionIntent intent : intents) {
            if (intent.getStatus() == IntentStatus.COMMITTED || intent.getStatus() == IntentStatus.CANCELLED) {
                log.info("Skipping already {} intent id={}", intent.getStatus(), intent.getId());
                continue;
            }

            if (intent.getStatus() == IntentStatus.READY) {
                intent.setStatus(IntentStatus.CANCELLED);
                transactionIntentRepository.save(intent);
                log.info("Intent id={} cancelled", intent.getId());
            }
        }
    }
}
