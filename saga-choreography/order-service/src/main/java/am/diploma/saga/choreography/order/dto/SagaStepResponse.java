package am.diploma.saga.choreography.order.dto;

import lombok.*;

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
    private LocalDateTime completedAt;
}
