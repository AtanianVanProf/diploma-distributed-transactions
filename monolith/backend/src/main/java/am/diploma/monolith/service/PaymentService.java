package am.diploma.monolith.service;

import am.diploma.monolith.exception.InsufficientBalanceException;
import am.diploma.monolith.exception.NotFoundException;
import am.diploma.monolith.entity.Customer;
import am.diploma.monolith.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final CustomerRepository customerRepository;

    public void processPayment(Long customerId, BigDecimal amount) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new NotFoundException("CUSTOMER_NOT_FOUND",
                        "Customer with ID " + customerId + " not found"));

        if (customer.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException(customerId, customer.getBalance(), amount);
        }

        customer.setBalance(customer.getBalance().subtract(amount));
    }
}
