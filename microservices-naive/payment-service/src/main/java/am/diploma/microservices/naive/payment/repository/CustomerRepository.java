package am.diploma.microservices.naive.payment.repository;

import am.diploma.microservices.naive.payment.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
}
