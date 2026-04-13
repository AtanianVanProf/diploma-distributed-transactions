package am.diploma.saga.choreography.payment.controller;

import am.diploma.saga.choreography.payment.entity.Customer;
import am.diploma.saga.choreography.payment.exception.NotFoundException;
import am.diploma.saga.choreography.payment.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerRepository customerRepository;

    @GetMapping
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    @GetMapping("/{id}")
    public Customer getCustomerById(@PathVariable Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("CUSTOMER_NOT_FOUND",
                        "Customer with ID " + id + " not found"));
    }
}
