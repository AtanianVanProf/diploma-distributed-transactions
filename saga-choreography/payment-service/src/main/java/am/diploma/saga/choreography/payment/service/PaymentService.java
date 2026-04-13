package am.diploma.saga.choreography.payment.service;

import am.diploma.saga.choreography.payment.entity.Customer;
import am.diploma.saga.choreography.payment.exception.InsufficientBalanceException;
import am.diploma.saga.choreography.payment.exception.NotFoundException;
import am.diploma.saga.choreography.payment.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final CustomerRepository customerRepository;

    @Transactional
    public void charge(Long customerId, BigDecimal amount) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new NotFoundException("CUSTOMER_NOT_FOUND",
                        "Customer with ID " + customerId + " not found"));

        if (customer.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException(
                    customer.getId(), customer.getBalance(), amount);
        }

        customer.setBalance(customer.getBalance().subtract(amount));
    }
}
