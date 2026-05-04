package am.diploma.pvip.protocol.inventory.dto;

import java.math.BigDecimal;

public record ValidationResponse(
        boolean valid,
        Long productId,
        String productName,
        Integer availableStock,
        Integer requestedQuantity,
        BigDecimal price,
        String reason
) {
}
