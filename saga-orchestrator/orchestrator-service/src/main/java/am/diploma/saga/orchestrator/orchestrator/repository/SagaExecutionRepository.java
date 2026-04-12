package am.diploma.saga.orchestrator.orchestrator.repository;

import am.diploma.saga.orchestrator.orchestrator.entity.SagaExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SagaExecutionRepository extends JpaRepository<SagaExecution, Long> {

    @Query("SELECT DISTINCT s FROM SagaExecution s LEFT JOIN FETCH s.steps ORDER BY s.id")
    List<SagaExecution> findAllWithSteps();
}
