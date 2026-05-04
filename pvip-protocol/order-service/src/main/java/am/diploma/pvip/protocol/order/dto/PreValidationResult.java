package am.diploma.pvip.protocol.order.dto;

import java.math.BigDecimal;
import java.util.List;

public record PreValidationResult(
        boolean passed,
        String reason,
        BigDecimal totalAmount,
        List<ValidatedItem> items,
        String customerName
) {

    public static PreValidationResult failed(String reason) {
        return new PreValidationResult(false, reason, null, null, null);
    }

    public static PreValidationResult passed(BigDecimal totalAmount, List<ValidatedItem> items, String customerName) {
        return new PreValidationResult(true, null, totalAmount, items, customerName);
    }
}
