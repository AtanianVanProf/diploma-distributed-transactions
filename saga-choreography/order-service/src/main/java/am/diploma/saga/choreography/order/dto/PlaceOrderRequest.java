package am.diploma.saga.choreography.order.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlaceOrderRequest {

    private Long customerId;
    private List<OrderItemRequest> items;
}
