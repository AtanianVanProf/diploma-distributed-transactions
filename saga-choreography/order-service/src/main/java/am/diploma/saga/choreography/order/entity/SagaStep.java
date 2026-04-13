package am.diploma.saga.choreography.order.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "saga_step")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SagaStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "saga_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private SagaExecution sagaExecution;

    @Column(name = "step_name", nullable = false, length = 50)
    private String stepName;

    @Column(name = "step_order", nullable = false)
    private Integer stepOrder;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StepStatus status;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}
