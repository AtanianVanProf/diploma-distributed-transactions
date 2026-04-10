package am.diploma.saga.orchestrator.payment.repository;

import am.diploma.saga.orchestrator.payment.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
}
