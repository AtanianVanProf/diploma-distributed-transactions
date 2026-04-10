package am.diploma.saga.orchestrator.orchestrator.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderRequest {
    private Long customerId;
    private String status;
    private BigDecimal totalAmount;
    private String failureReason;
    private List<CreateOrderItemRequest> items;
}
