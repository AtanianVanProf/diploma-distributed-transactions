package am.diploma.pvip.protocol.commitgate.repository;

import am.diploma.pvip.protocol.commitgate.entity.ParticipantResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ParticipantResponseRepository extends JpaRepository<ParticipantResponse, Long> {

    Optional<ParticipantResponse> findByTransactionIdAndParticipantType(UUID transactionId, String participantType);

    List<ParticipantResponse> findByTransactionId(UUID transactionId);
}
