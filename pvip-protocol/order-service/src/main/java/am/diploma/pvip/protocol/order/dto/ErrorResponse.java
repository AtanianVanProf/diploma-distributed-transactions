package am.diploma.pvip.protocol.order.dto;

import java.util.Map;

public record ErrorResponse(String error, String message, Map<String, Object> details) {}
