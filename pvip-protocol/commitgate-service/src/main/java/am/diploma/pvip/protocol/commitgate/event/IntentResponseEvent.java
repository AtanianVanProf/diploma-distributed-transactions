package am.diploma.pvip.protocol.commitgate.event;

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
public class IntentResponseEvent {

    private UUID transactionId;
    private String participantType;
    private String status;
    private String reason;

    private Long productId;
    private String productName;
    private Integer availableStock;
    private BigDecimal price;

    private Long customerId;
    private String customerName;
    private BigDecimal availableBalance;
}
