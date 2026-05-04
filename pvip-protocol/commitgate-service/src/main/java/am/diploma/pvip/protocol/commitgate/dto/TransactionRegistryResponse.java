package am.diploma.pvip.protocol.commitgate.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record TransactionRegistryResponse(
        UUID transactionId,
        String status,
        Integer expectedParticipants,
        Integer receivedResponses,
        String decision,
        String decisionReason,
        LocalDateTime createdAt,
        LocalDateTime decidedAt,
        List<ParticipantResponseDto> participants
) {
}
