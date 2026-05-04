package am.diploma.pvip.protocol.inventory.dto;

import java.util.Map;

public record ErrorResponse(
        String error,
        String message,
        Map<String, Object> details
) {
}
