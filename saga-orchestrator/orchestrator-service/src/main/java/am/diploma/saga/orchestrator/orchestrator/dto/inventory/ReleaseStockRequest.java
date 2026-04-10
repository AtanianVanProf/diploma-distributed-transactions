package am.diploma.saga.orchestrator.orchestrator.dto.inventory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReleaseStockRequest {
    private List<StockItemRequest> items;
}
