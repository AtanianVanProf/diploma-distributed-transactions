package am.diploma.pvip.protocol.commitgate.dto;

import java.time.LocalDateTime;

public record ParticipantResponseDto(
        String participantType,
        String status,
        String reason,
        LocalDateTime receivedAt
) {
}
