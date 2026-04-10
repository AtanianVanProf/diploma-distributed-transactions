package am.diploma.saga.orchestrator.orchestrator.repository;

import am.diploma.saga.orchestrator.orchestrator.entity.SagaStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SagaStepRepository extends JpaRepository<SagaStep, Long> {
}
