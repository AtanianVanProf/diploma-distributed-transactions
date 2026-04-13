package am.diploma.saga.choreography.order.dto;

import lombok.*;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {
    private String error;
    private String message;
    private Map<String, Object> details;
}
