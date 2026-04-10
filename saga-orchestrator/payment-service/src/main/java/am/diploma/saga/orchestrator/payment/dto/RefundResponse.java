package am.diploma.saga.orchestrator.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefundResponse {
    private boolean refunded;
    private Long customerId;
    private BigDecimal amount;
    private BigDecimal newBalance;
}
