package am.diploma.pvip.protocol.commitgate.service;

import am.diploma.pvip.protocol.commitgate.dto.ParticipantResponseDto;
import am.diploma.pvip.protocol.commitgate.dto.TransactionRegistryResponse;
import am.diploma.pvip.protocol.commitgate.entity.TransactionRegistry;
import am.diploma.pvip.protocol.commitgate.exception.NotFoundException;
import am.diploma.pvip.protocol.commitgate.repository.ParticipantResponseRepository;
import am.diploma.pvip.protocol.commitgate.repository.TransactionRegistryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRegistryRepository registryRepository;
    private final ParticipantResponseRepository responseRepository;

    @Transactional(readOnly = true)
    public List<TransactionRegistryResponse> getAllTransactions() {
        return registryRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TransactionRegistryResponse getTransaction(UUID transactionId) {
        TransactionRegistry registry = registryRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new NotFoundException("TRANSACTION_NOT_FOUND",
                        "Transaction not found: " + transactionId));
        return toResponse(registry);
    }

    private TransactionRegistryResponse toResponse(TransactionRegistry registry) {
        List<ParticipantResponseDto> participants = responseRepository
                .findByTransactionId(registry.getTransactionId())
                .stream()
                .map(r -> new ParticipantResponseDto(
                        r.getParticipantType(),
                        r.getStatus(),
                        r.getReason(),
                        r.getReceivedAt()))
                .toList();

        return new TransactionRegistryResponse(
                registry.getTransactionId(),
                registry.getStatus().name(),
                registry.getExpectedParticipants(),
                registry.getReceivedResponses(),
                registry.getDecision() != null ? registry.getDecision().name() : null,
                registry.getDecisionReason(),
                registry.getCreatedAt(),
                registry.getDecidedAt(),
                participants
        );
    }
}
