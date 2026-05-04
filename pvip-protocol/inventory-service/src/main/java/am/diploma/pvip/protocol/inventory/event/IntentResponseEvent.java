package am.diploma.pvip.protocol.inventory.event;

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
}
