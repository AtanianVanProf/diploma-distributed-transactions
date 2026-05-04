package am.diploma.pvip.protocol.order.client;

import java.math.BigDecimal;

public record PaymentValidationResponse(
        boolean valid,
        Long customerId,
        String customerName,
        BigDecimal availableBalance,
        BigDecimal requestedAmount,
        String reason
) {}
