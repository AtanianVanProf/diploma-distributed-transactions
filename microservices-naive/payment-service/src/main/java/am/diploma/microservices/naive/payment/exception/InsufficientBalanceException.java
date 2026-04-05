package am.diploma.microservices.naive.payment.exception;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class InsufficientBalanceException extends RuntimeException {

    private final Long customerId;
    private final BigDecimal available;
    private final BigDecimal requested;

    public InsufficientBalanceException(Long customerId, BigDecimal available, BigDecimal requested) {
        super(String.format("Insufficient balance for customer %d. Available: %s, requested: %s",
                customerId, available, requested));
        this.customerId = customerId;
        this.available = available;
        this.requested = requested;
    }
}
