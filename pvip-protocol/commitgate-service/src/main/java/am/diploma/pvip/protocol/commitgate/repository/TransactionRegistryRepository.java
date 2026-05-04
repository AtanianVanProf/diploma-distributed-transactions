package am.diploma.pvip.protocol.commitgate.repository;

import am.diploma.pvip.protocol.commitgate.entity.TransactionRegistry;
import am.diploma.pvip.protocol.commitgate.entity.TransactionStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRegistryRepository extends JpaRepository<TransactionRegistry, Long> {

    Optional<TransactionRegistry> findByTransactionId(UUID transactionId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM TransactionRegistry r WHERE r.transactionId = :transactionId")
    Optional<TransactionRegistry> findByTransactionIdForUpdate(@Param("transactionId") UUID transactionId);

    List<TransactionRegistry> findByStatusAndCreatedAtBefore(TransactionStatus status, LocalDateTime before);

    List<TransactionRegistry> findAllByOrderByCreatedAtDesc();
}
