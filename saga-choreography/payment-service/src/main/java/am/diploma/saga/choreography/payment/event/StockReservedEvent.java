package am.diploma.saga.choreography.payment.event;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockReservedEvent {

    private Long sagaId;
    private Long orderId;
    private Long customerId;
    private List<ReservedItem> items;
    private BigDecimal totalAmount;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReservedItem {
        private Long productId;
        private String productName;
        private Integer quantity;
        private BigDecimal price;
    }
}
