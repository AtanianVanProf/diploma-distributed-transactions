package am.diploma.saga.choreography.inventory.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderFailedEvent {

    private Long sagaId;
    private Long orderId;
    private String reason;
}
