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
public class ReleaseStockResponse {
    private boolean released;
    private List<ReleasedItemResponse> items;
}
