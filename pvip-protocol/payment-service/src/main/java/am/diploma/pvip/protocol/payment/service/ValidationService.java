package am.diploma.pvip.protocol.payment.service;

import am.diploma.pvip.protocol.payment.dto.ValidationResponse;
import am.diploma.pvip.protocol.payment.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ValidationService {

    private final CustomerRepository customerRepository;

    @Transactional(readOnly = true)
    public ValidationResponse validateBalance(Long customerId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return new ValidationResponse(false, customerId, null, null, amount, "Amount must be positive");
        }

        var customerOpt = customerRepository.findById(customerId);

        if (customerOpt.isEmpty()) {
            return new ValidationResponse(false, customerId, null, null, amount, "Customer not found");
        }

        var customer = customerOpt.get();

        if (customer.getBalance().compareTo(amount) < 0) {
            return new ValidationResponse(
                    false,
                    customer.getId(),
                    customer.getName(),
                    customer.getBalance(),
                    amount,
                    String.format("Insufficient balance: available %s, requested %s",
                            customer.getBalance(), amount)
            );
        }

        return new ValidationResponse(
                true,
                customer.getId(),
                customer.getName(),
                customer.getBalance(),
                amount,
                null
        );
    }
}
