package am.diploma.pvip.protocol.order.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "protocol_execution")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProtocolExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private UUID transactionId;

    private Long orderId;

    @Enumerated(EnumType.STRING)
    private ProtocolStatus status;

    private String phase;

    private Integer kafkaMessagesSent;

    private Integer compensationsTriggered;

    private Boolean preValidationPassed;

    private Integer intentResponsesReceived;

    private String decision;

    private String decisionReason;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        startedAt = LocalDateTime.now();
        if (kafkaMessagesSent == null) kafkaMessagesSent = 0;
        if (compensationsTriggered == null) compensationsTriggered = 0;
        if (intentResponsesReceived == null) intentResponsesReceived = 0;
    }
}
