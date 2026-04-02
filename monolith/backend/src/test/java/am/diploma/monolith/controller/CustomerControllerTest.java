package am.diploma.monolith.controller;

import am.diploma.monolith.entity.Customer;
import am.diploma.monolith.repository.CustomerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CustomerController.class)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CustomerRepository customerRepository;

    @Test
    @DisplayName("GET /api/customers returns 200 with list of all customers")
    void getAllCustomers_returns200WithCustomerList() throws Exception {
        Customer alice = Customer.builder().id(1L).name("Alice Johnson")
                .email("alice@example.com").balance(new BigDecimal("10000.00")).build();
        Customer bob = Customer.builder().id(2L).name("Bob Smith")
                .email("bob@example.com").balance(new BigDecimal("50.00")).build();

        when(customerRepository.findAll()).thenReturn(List.of(alice, bob));

        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Alice Johnson"))
                .andExpect(jsonPath("$[0].email").value("alice@example.com"))
                .andExpect(jsonPath("$[0].balance").value(10000.00))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Bob Smith"));
    }

    @Test
    @DisplayName("GET /api/customers/{id} returns 200 with customer data including balance")
    void getCustomerById_validId_returns200WithCustomer() throws Exception {
        Customer alice = Customer.builder().id(1L).name("Alice Johnson")
                .email("alice@example.com").balance(new BigDecimal("10000.00")).build();

        when(customerRepository.findById(1L)).thenReturn(Optional.of(alice));

        mockMvc.perform(get("/api/customers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Alice Johnson"))
                .andExpect(jsonPath("$.email").value("alice@example.com"))
                .andExpect(jsonPath("$.balance").value(10000.00));
    }

    @Test
    @DisplayName("GET /api/customers/{id} returns 404 with CUSTOMER_NOT_FOUND for non-existent ID")
    void getCustomerById_invalidId_returns404() throws Exception {
        when(customerRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/customers/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("CUSTOMER_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Customer with ID 999 not found"));
    }
}
