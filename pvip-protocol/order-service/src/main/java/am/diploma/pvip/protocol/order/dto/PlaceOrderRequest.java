package am.diploma.pvip.protocol.order.dto;

import java.util.List;

public record PlaceOrderRequest(Long customerId, List<OrderItemRequest> items) {}
