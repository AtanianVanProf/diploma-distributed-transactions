package am.diploma.saga.orchestrator.inventory.exception;

import lombok.Getter;

@Getter
public class InsufficientStockException extends RuntimeException {

    private final Long productId;
    private final String productName;
    private final int available;
    private final int requested;

    public InsufficientStockException(Long productId, String productName, int available, int requested) {
        super(String.format("Not enough stock for product '%s'. Available: %d, requested: %d",
                productName, available, requested));
        this.productId = productId;
        this.productName = productName;
        this.available = available;
        this.requested = requested;
    }
}
