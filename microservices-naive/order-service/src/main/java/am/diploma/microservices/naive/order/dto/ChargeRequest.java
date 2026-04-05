package am.diploma.microservices.naive.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChargeRequest {

    private Long customerId;
    private BigDecimal amount;
}
