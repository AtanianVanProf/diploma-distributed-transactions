package am.diploma.saga.choreography.order.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlaceOrderResponse {

    private Long orderId;
    private Long sagaId;
}
