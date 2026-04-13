package am.diploma.saga.choreography.payment.event;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockReservationFailedEvent {

    private Long sagaId;
    private Long orderId;
    private String reason;
}
