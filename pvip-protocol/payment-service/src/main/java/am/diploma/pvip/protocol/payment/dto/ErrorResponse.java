package am.diploma.pvip.protocol.payment.dto;

import java.util.Map;

public record ErrorResponse(
        String error,
        String message,
        Map<String, Object> details
) {
}
