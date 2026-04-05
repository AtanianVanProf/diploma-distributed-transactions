package am.diploma.microservices.naive.payment.service;

import am.diploma.microservices.naive.payment.dto.ChargeRequest;
import am.diploma.microservices.naive.payment.dto.ChargeResponse;
import am.diploma.microservices.naive.payment.entity.Customer;
import am.diploma.microservices.naive.payment.exception.InsufficientBalanceException;
import am.diploma.microservices.naive.payment.exception.NotFoundException;
import am.diploma.microservices.naive.payment.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final CustomerRepository customerRepository;

    @Transactional
    public ChargeResponse charge(ChargeRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new NotFoundException("CUSTOMER_NOT_FOUND",
                        "Customer with ID " + request.getCustomerId() + " not found"));

        if (customer.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException(
                    customer.getId(), customer.getBalance(), request.getAmount());
        }

        customer.setBalance(customer.getBalance().subtract(request.getAmount()));

        return ChargeResponse.builder()
                .charged(true)
                .customerId(customer.getId())
                .amount(request.getAmount())
                .remainingBalance(customer.getBalance())
                .build();
    }
}
