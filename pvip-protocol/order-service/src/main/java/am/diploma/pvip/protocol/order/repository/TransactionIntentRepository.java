package am.diploma.pvip.protocol.order.repository;

import am.diploma.pvip.protocol.order.entity.TransactionIntent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionIntentRepository extends JpaRepository<TransactionIntent, Long> {

    List<TransactionIntent> findByTransactionId(UUID transactionId);
}
