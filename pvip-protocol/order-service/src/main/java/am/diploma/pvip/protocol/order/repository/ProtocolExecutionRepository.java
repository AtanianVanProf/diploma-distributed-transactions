package am.diploma.pvip.protocol.order.repository;

import am.diploma.pvip.protocol.order.entity.ProtocolExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProtocolExecutionRepository extends JpaRepository<ProtocolExecution, Long> {

    Optional<ProtocolExecution> findByTransactionId(UUID transactionId);

    List<ProtocolExecution> findAllByOrderByStartedAtDesc();
}
