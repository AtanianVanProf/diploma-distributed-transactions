package am.diploma.saga.choreography.payment.event;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

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
