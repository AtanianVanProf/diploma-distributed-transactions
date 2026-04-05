package am.diploma.microservices.naive.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockItemRequest {
    private Long productId;
    private Integer quantity;
}
