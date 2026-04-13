package am.diploma.saga.choreography.inventory.event;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderPlacedEvent {

    private Long sagaId;
    private Long orderId;
    private Long customerId;
    private List<OrderItem> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderItem {

        private Long productId;
        private Integer quantity;
    }
}
