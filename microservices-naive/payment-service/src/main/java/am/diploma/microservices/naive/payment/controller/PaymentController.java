package am.diploma.microservices.naive.payment.controller;

import am.diploma.microservices.naive.payment.dto.ChargeRequest;
import am.diploma.microservices.naive.payment.dto.ChargeResponse;
import am.diploma.microservices.naive.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/charge")
    public ChargeResponse charge(@RequestBody ChargeRequest request) {
        return paymentService.charge(request);
    }
}
