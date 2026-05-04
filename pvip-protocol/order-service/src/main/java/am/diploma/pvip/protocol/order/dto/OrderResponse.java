package am.diploma.pvip.protocol.order.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        Long id,
        UUID transactionId,
        Long customerId,
        String customerName,
        String status,
        BigDecimal totalAmount,
        String failureReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<OrderItemResponse> items
) {}
