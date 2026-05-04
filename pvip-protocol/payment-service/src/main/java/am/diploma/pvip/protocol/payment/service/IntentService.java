package am.diploma.pvip.protocol.payment.service;

import am.diploma.pvip.protocol.payment.entity.Customer;
import am.diploma.pvip.protocol.payment.entity.IntentStatus;
import am.diploma.pvip.protocol.payment.entity.TransactionIntent;
import am.diploma.pvip.protocol.payment.exception.NotFoundException;
import am.diploma.pvip.protocol.payment.repository.CustomerRepository;
import am.diploma.pvip.protocol.payment.repository.TransactionIntentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class IntentService {

    private final TransactionIntentRepository transactionIntentRepository;
    private final CustomerRepository customerRepository;

    @Transactional
    public TransactionIntent registerIntent(UUID transactionId, Long customerId, BigDecimal amount) {
        log.info("Registering intent for transaction={}, customer={}, amount={}", transactionId, customerId, amount);

        // Idempotency check
        var existing = transactionIntentRepository.findByTransactionIdAndCustomerId(transactionId, customerId);
        if (existing.isPresent()) {
            log.info("Intent already exists for transaction={}, customer={}", transactionId, customerId);
            return existing.get();
        }

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new NotFoundException("CUSTOMER_NOT_FOUND", "Customer not found: " + customerId));

        TransactionIntent intent;
        if (customer.getBalance().compareTo(amount) >= 0) {
            intent = TransactionIntent.builder()
                    .transactionId(transactionId)
                    .customerId(customerId)
                    .amount(amount)
                    .status(IntentStatus.READY)
                    .build();
            log.info("Intent READY for transaction={}, customer={}", transactionId, customerId);
        } else {
            String reason = String.format("Insufficient balance: available %s, requested %s",
                    customer.getBalance(), amount);
            intent = TransactionIntent.builder()
                    .transactionId(transactionId)
                    .customerId(customerId)
                    .amount(amount)
                    .status(IntentStatus.FAILED)
                    .reason(reason)
                    .build();
            log.warn("Intent FAILED for transaction={}, customer={}: {}", transactionId, customerId, reason);
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
                Customer customer = customerRepository.findByIdForUpdate(intent.getCustomerId())
                        .orElseThrow(() -> new NotFoundException("CUSTOMER_NOT_FOUND",
                                "Customer not found: " + intent.getCustomerId()));

                if (customer.getBalance().compareTo(intent.getAmount()) < 0) {
                    intent.setStatus(IntentStatus.FAILED);
                    intent.setReason("Insufficient balance at commit time");
                    transactionIntentRepository.save(intent);
                    log.warn("Intent id={} failed at commit: balance={}, requested={}",
                            intent.getId(), customer.getBalance(), intent.getAmount());
                    continue;
                }

                customer.setBalance(customer.getBalance().subtract(intent.getAmount()));
                customerRepository.save(customer);

                intent.setStatus(IntentStatus.COMMITTED);
                transactionIntentRepository.save(intent);
                log.info("Intent id={} committed, deducted {} from customer={}",
                        intent.getId(), intent.getAmount(), intent.getCustomerId());
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
