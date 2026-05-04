package am.diploma.pvip.protocol.order.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record PlaceOrderResponse(
        Long orderId,
        UUID transactionId,
        String status,
        String phase,
        Integer kafkaMessages,
        Integer compensations,
        String reason,
        BigDecimal totalAmount
) {}
