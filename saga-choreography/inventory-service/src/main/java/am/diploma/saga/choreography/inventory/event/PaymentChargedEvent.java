package am.diploma.saga.choreography.inventory.event;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentChargedEvent {

    private Long sagaId;
    private Long orderId;
    private Long customerId;
    private BigDecimal amount;
}
