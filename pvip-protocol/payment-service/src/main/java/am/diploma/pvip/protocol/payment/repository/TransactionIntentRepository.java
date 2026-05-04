package am.diploma.pvip.protocol.payment.repository;

import am.diploma.pvip.protocol.payment.entity.TransactionIntent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionIntentRepository extends JpaRepository<TransactionIntent, Long> {

    Optional<TransactionIntent> findByTransactionIdAndCustomerId(UUID transactionId, Long customerId);

    List<TransactionIntent> findByTransactionId(UUID transactionId);
}
