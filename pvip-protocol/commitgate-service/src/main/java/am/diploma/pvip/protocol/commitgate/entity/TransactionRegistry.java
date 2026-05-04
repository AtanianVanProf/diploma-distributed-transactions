package am.diploma.pvip.protocol.commitgate.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transaction_registry")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRegistry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private UUID transactionId;

    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    private Integer expectedParticipants;
    private Integer receivedResponses;

    @Enumerated(EnumType.STRING)
    private DecisionType decision;

    private String decisionReason;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "decided_at")
    private LocalDateTime decidedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (receivedResponses == null) {
            receivedResponses = 0;
        }
    }
}
