package am.diploma.saga.orchestrator.payment.controller;

import am.diploma.saga.orchestrator.payment.dto.ChargeRequest;
import am.diploma.saga.orchestrator.payment.dto.ChargeResponse;
import am.diploma.saga.orchestrator.payment.dto.RefundRequest;
import am.diploma.saga.orchestrator.payment.dto.RefundResponse;
import am.diploma.saga.orchestrator.payment.service.PaymentService;
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

    @PostMapping("/refund")
    public RefundResponse refund(@RequestBody RefundRequest request) {
        return paymentService.refund(request);
    }
}
