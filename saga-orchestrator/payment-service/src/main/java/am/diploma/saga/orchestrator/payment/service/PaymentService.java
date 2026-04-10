package am.diploma.saga.orchestrator.payment.service;

import am.diploma.saga.orchestrator.payment.dto.ChargeRequest;
import am.diploma.saga.orchestrator.payment.dto.ChargeResponse;
import am.diploma.saga.orchestrator.payment.dto.RefundRequest;
import am.diploma.saga.orchestrator.payment.dto.RefundResponse;
import am.diploma.saga.orchestrator.payment.entity.Customer;
import am.diploma.saga.orchestrator.payment.exception.InsufficientBalanceException;
import am.diploma.saga.orchestrator.payment.exception.NotFoundException;
import am.diploma.saga.orchestrator.payment.repository.CustomerRepository;
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

    @Transactional
    public RefundResponse refund(RefundRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new NotFoundException("CUSTOMER_NOT_FOUND",
                        "Customer with ID " + request.getCustomerId() + " not found"));

        customer.setBalance(customer.getBalance().add(request.getAmount()));

        return RefundResponse.builder()
                .refunded(true)
                .customerId(customer.getId())
                .amount(request.getAmount())
                .newBalance(customer.getBalance())
                .build();
    }
}
