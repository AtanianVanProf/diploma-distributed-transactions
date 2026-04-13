package am.diploma.saga.choreography.order.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SagaExecutionResponse {

    private Long id;
    private Long orderId;
    private String status;
    private String requestPayload;
    private String failureReason;
    private List<SagaStepResponse> steps;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
