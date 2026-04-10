package am.diploma.saga.orchestrator.orchestrator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SagaStepResponse {
    private Long id;
    private String stepName;
    private Integer stepOrder;
    private String status;
    private String requestData;
    private String responseData;
    private String compensationData;
    private LocalDateTime completedAt;
}
