package am.diploma.pvip.protocol.payment.dto;

import java.math.BigDecimal;

public record ValidationResponse(
        boolean valid,
        Long customerId,
        String customerName,
        BigDecimal availableBalance,
        BigDecimal requestedAmount,
        String reason
) {
}
