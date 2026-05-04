package am.diploma.pvip.protocol.inventory.repository;

import am.diploma.pvip.protocol.inventory.entity.TransactionIntent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionIntentRepository extends JpaRepository<TransactionIntent, Long> {

    Optional<TransactionIntent> findByTransactionIdAndProductId(UUID transactionId, Long productId);

    List<TransactionIntent> findByTransactionId(UUID transactionId);
}
