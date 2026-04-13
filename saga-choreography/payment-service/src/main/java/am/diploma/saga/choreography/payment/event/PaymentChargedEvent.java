package am.diploma.saga.choreography.payment.event;

import lombok.*;

import java.math.BigDecimal;

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
