package am.diploma.saga.choreography.payment.event;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderFailedEvent {

    private Long sagaId;
    private Long orderId;
    private String reason;
}
