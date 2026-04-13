package am.diploma.saga.choreography.inventory.event;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderCompletedEvent {

    private Long sagaId;
    private Long orderId;
    private Long customerId;
    private List<CompletedItem> items;
    private BigDecimal totalAmount;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CompletedItem {

        private Long productId;
        private String productName;
        private Integer quantity;
        private BigDecimal price;
    }
}
