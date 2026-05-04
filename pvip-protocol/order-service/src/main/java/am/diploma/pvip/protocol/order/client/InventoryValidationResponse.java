package am.diploma.pvip.protocol.order.client;

import java.math.BigDecimal;

public record InventoryValidationResponse(
        boolean valid,
        Long productId,
        String productName,
        Integer availableStock,
        Integer requestedQuantity,
        BigDecimal price,
        String reason
) {}
