package am.diploma.saga.orchestrator.orchestrator.dto.inventory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockItemRequest {
    private Long productId;
    private Integer quantity;
}
