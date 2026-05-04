package am.diploma.pvip.protocol.inventory.controller;

import am.diploma.pvip.protocol.inventory.dto.ValidationResponse;
import am.diploma.pvip.protocol.inventory.service.ValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class ValidationController {

    private final ValidationService validationService;

    @GetMapping("/validate")
    public ResponseEntity<ValidationResponse> validateStock(
            @RequestParam Long productId,
            @RequestParam Integer quantity) {
        return ResponseEntity.ok(validationService.validateStock(productId, quantity));
    }
}
