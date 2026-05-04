package am.diploma.pvip.protocol.order.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentValidationClient {

    private final RestClient paymentRestClient;

    public PaymentValidationResponse validate(Long customerId, BigDecimal amount) {
        log.info("Validating payment: customerId={}, amount={}", customerId, amount);
        return paymentRestClient.get()
                .uri("/api/payment/validate?customerId={customerId}&amount={amount}", customerId, amount)
                .retrieve()
                .body(PaymentValidationResponse.class);
    }
}
