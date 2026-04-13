package am.diploma.saga.choreography.order.repository;

import am.diploma.saga.choreography.order.entity.SagaStep;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SagaStepRepository extends JpaRepository<SagaStep, Long> {
}
