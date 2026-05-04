package am.diploma.pvip.protocol.order.service;

import am.diploma.pvip.protocol.order.client.InventoryValidationClient;
import am.diploma.pvip.protocol.order.client.InventoryValidationResponse;
import am.diploma.pvip.protocol.order.client.PaymentValidationClient;
import am.diploma.pvip.protocol.order.client.PaymentValidationResponse;
import am.diploma.pvip.protocol.order.dto.OrderItemRequest;
import am.diploma.pvip.protocol.order.dto.PreValidationResult;
import am.diploma.pvip.protocol.order.dto.ValidatedItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PreValidationService {

    private final InventoryValidationClient inventoryClient;
    private final PaymentValidationClient paymentClient;

    public PreValidationResult validate(Long customerId, List<OrderItemRequest> items) {
        log.info("Starting pre-validation for customerId={}, items={}", customerId, items.size());

        List<ValidatedItem> validatedItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderItemRequest item : items) {
            InventoryValidationResponse response;
            try {
                response = inventoryClient.validate(item.productId(), item.quantity());
            } catch (RestClientException e) {
                log.error("Inventory service unavailable: {}", e.getMessage());
                return PreValidationResult.failed("Inventory service unavailable");
            }
            if (!response.valid()) {
                log.warn("Inventory pre-validation failed: {}", response.reason());
                return PreValidationResult.failed(response.reason());
            }
            validatedItems.add(new ValidatedItem(response.productId(), response.productName(), item.quantity(), response.price()));
            totalAmount = totalAmount.add(response.price().multiply(BigDecimal.valueOf(item.quantity())));
        }

        PaymentValidationResponse paymentResponse;
        try {
            paymentResponse = paymentClient.validate(customerId, totalAmount);
        } catch (RestClientException e) {
            log.error("Payment service unavailable: {}", e.getMessage());
            return PreValidationResult.failed("Payment service unavailable");
        }
        if (!paymentResponse.valid()) {
            log.warn("Payment pre-validation failed: {}", paymentResponse.reason());
            return PreValidationResult.failed(paymentResponse.reason());
        }

        log.info("Pre-validation passed, totalAmount={}", totalAmount);
        return PreValidationResult.passed(totalAmount, validatedItems, paymentResponse.customerName());
    }
}
