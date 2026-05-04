package am.diploma.pvip.protocol.inventory.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GlobalDecisionEvent {

    private UUID transactionId;
    private String decision;
    private String reason;
}
