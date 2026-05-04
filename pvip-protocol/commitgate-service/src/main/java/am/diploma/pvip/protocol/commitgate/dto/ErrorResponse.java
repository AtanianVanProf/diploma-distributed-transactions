package am.diploma.pvip.protocol.commitgate.dto;

import java.util.Map;

public record ErrorResponse(
        String error,
        String message,
        Map<String, Object> details
) {
}
