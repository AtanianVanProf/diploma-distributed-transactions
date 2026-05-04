package am.diploma.pvip.protocol.payment.controller;

import am.diploma.pvip.protocol.payment.dto.ValidationResponse;
import am.diploma.pvip.protocol.payment.service.ValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class ValidationController {

    private final ValidationService validationService;

    @GetMapping("/validate")
    public ResponseEntity<ValidationResponse> validateBalance(
            @RequestParam Long customerId,
            @RequestParam BigDecimal amount) {
        ValidationResponse response = validationService.validateBalance(customerId, amount);
        return ResponseEntity.ok(response);
    }
}
