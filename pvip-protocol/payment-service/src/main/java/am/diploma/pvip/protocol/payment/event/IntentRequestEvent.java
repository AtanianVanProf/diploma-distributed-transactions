package am.diploma.pvip.protocol.payment.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntentRequestEvent {

    private UUID transactionId;
    private String participantType;
    private Long productId;
    private Integer quantity;
    private Long customerId;
    private BigDecimal amount;
}
