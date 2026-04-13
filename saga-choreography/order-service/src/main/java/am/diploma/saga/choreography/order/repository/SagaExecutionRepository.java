package am.diploma.saga.choreography.order.repository;

import am.diploma.saga.choreography.order.entity.SagaExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SagaExecutionRepository extends JpaRepository<SagaExecution, Long> {

    @Query("SELECT DISTINCT s FROM SagaExecution s LEFT JOIN FETCH s.steps ORDER BY s.id DESC")
    List<SagaExecution> findAllWithSteps();

    @Query("SELECT s FROM SagaExecution s LEFT JOIN FETCH s.steps WHERE s.id = :id")
    Optional<SagaExecution> findByIdWithSteps(@Param("id") Long id);
}
