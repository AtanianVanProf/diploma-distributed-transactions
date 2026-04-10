package am.diploma.saga.orchestrator.orchestrator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
