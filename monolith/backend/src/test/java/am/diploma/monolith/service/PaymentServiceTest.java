package am.diploma.monolith.service;

import am.diploma.monolith.entity.Customer;
import am.diploma.monolith.exception.InsufficientBalanceException;
import am.diploma.monolith.exception.NotFoundException;
import am.diploma.monolith.repository.CustomerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    @DisplayName("Successfully processes payment and deducts balance")
    void processPayment_sufficientBalance_deductsAmount() {
        Customer customer = Customer.builder()
                .id(1L)
                .name("Alice")
                .email("alice@example.com")
                .balance(new BigDecimal("10000.00"))
                .build();
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        paymentService.processPayment(1L, new BigDecimal("2999.98"));

        assertEquals(new BigDecimal("7000.02"), customer.getBalance());
    }

    @Test
    @DisplayName("Throws NotFoundException when customer does not exist")
    void processPayment_customerNotFound_throwsNotFoundException() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> paymentService.processPayment(99L, new BigDecimal("100.00")));

        assertEquals("CUSTOMER_NOT_FOUND", ex.getErrorCode());
        assertEquals("Customer with ID 99 not found", ex.getMessage());
    }

    @Test
    @DisplayName("Throws InsufficientBalanceException when balance is less than requested amount")
    void processPayment_insufficientBalance_throwsInsufficientBalanceException() {
        Customer customer = Customer.builder()
                .id(2L)
                .name("Bob")
                .email("bob@example.com")
                .balance(new BigDecimal("50.00"))
                .build();
        when(customerRepository.findById(2L)).thenReturn(Optional.of(customer));

        InsufficientBalanceException ex = assertThrows(InsufficientBalanceException.class,
                () -> paymentService.processPayment(2L, new BigDecimal("1299.99")));

        assertEquals(2L, ex.getCustomerId());
        assertEquals(new BigDecimal("50.00"), ex.getAvailable());
        assertEquals(new BigDecimal("1299.99"), ex.getRequested());
    }
}
