package am.diploma.benchmark.dto;

import java.util.List;

public record PlaceOrderRequest(
        Long customerId,
        List<OrderItemRequest> items
) {}
