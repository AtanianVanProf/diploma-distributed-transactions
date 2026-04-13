package am.diploma.saga.choreography.order.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {

    private Long orderId;
    private Long customerId;
    private String status;
    private BigDecimal totalAmount;
    private String failureReason;
    private List<OrderItemResponse> items;
    private LocalDateTime createdAt;
}
