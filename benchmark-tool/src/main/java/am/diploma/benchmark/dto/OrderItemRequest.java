package am.diploma.benchmark.dto;

public record OrderItemRequest(
        Long productId,
        Integer quantity
) {}
