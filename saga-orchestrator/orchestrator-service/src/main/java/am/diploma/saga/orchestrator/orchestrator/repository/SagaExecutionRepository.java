package am.diploma.saga.orchestrator.orchestrator.repository;

import am.diploma.saga.orchestrator.orchestrator.entity.SagaExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SagaExecutionRepository extends JpaRepository<SagaExecution, Long> {

    @Query("SELECT DISTINCT s FROM SagaExecution s LEFT JOIN FETCH s.steps")
    List<SagaExecution> findAllWithSteps();

    @Query("SELECT s FROM SagaExecution s LEFT JOIN FETCH s.steps WHERE s.id = :id")
    Optional<SagaExecution> findByIdWithSteps(@Param("id") Long id);
}
