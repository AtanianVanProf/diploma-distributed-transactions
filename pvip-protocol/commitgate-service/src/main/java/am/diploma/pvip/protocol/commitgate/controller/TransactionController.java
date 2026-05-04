package am.diploma.pvip.protocol.commitgate.controller;

import am.diploma.pvip.protocol.commitgate.dto.TransactionRegistryResponse;
import am.diploma.pvip.protocol.commitgate.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping
    public List<TransactionRegistryResponse> getAllTransactions() {
        return transactionService.getAllTransactions();
    }

    @GetMapping("/{transactionId}")
    public TransactionRegistryResponse getTransaction(@PathVariable UUID transactionId) {
        return transactionService.getTransaction(transactionId);
    }
}
