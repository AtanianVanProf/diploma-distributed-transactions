package am.diploma.pvip.protocol.order.exception;

import lombok.Getter;

@Getter
public class PreValidationException extends RuntimeException {

    private final String reason;
    private final String phase;

    public PreValidationException(String reason, String phase) {
        super(reason);
        this.reason = reason;
        this.phase = phase;
    }
}
