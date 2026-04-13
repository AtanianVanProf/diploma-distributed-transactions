package am.diploma.saga.choreography.payment.repository;

import am.diploma.saga.choreography.payment.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
}
