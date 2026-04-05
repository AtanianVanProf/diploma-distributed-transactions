package am.diploma.microservices.naive.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChargeResponse {
    private boolean charged;
    private Long customerId;
    private BigDecimal amount;
    private BigDecimal remainingBalance;
}
