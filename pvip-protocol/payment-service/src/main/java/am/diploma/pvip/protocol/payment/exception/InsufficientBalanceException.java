package am.diploma.pvip.protocol.payment.exception;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class InsufficientBalanceException extends RuntimeException {

    private final Long customerId;
    private final String customerName;
    private final BigDecimal available;
    private final BigDecimal requested;

    public InsufficientBalanceException(Long customerId, String customerName, BigDecimal available, BigDecimal requested) {
        super(String.format("Insufficient balance for customer %s: available=%s, requested=%s", customerName, available, requested));
        this.customerId = customerId;
        this.customerName = customerName;
        this.available = available;
        this.requested = requested;
    }
}
