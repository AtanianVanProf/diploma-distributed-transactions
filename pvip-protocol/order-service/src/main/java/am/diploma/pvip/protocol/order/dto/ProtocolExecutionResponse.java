package am.diploma.pvip.protocol.order.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ProtocolExecutionResponse(
        UUID transactionId,
        Long orderId,
        String status,
        String phase,
        Integer kafkaMessagesSent,
        Integer compensationsTriggered,
        Boolean preValidationPassed,
        Integer intentResponsesReceived,
        String decision,
        String decisionReason,
        LocalDateTime startedAt,
        LocalDateTime completedAt
) {}
