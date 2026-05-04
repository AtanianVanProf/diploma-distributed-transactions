package am.diploma.pvip.protocol.commitgate.service;

import am.diploma.pvip.protocol.commitgate.entity.DecisionType;
import am.diploma.pvip.protocol.commitgate.entity.ParticipantResponse;
import am.diploma.pvip.protocol.commitgate.entity.TransactionRegistry;
import am.diploma.pvip.protocol.commitgate.entity.TransactionStatus;
import am.diploma.pvip.protocol.commitgate.event.GlobalDecisionEvent;
import am.diploma.pvip.protocol.commitgate.event.IntentResponseEvent;
import am.diploma.pvip.protocol.commitgate.kafka.DecisionProducer;
import am.diploma.pvip.protocol.commitgate.repository.ParticipantResponseRepository;
import am.diploma.pvip.protocol.commitgate.repository.TransactionRegistryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommitGateService {

    private final TransactionRegistryRepository registryRepository;
    private final ParticipantResponseRepository responseRepository;
    private final DecisionProducer decisionProducer;

    @Value("${pvip.expected-participants}")
    private int expectedParticipants;

    @Transactional
    public void processIntentResponse(IntentResponseEvent event) {
        log.info("Processing intent response for transaction={}, participant={}, status={}",
                event.getTransactionId(), event.getParticipantType(), event.getStatus());

        TransactionRegistry registry = registryRepository.findByTransactionIdForUpdate(event.getTransactionId())
                .orElseGet(() -> {
                    TransactionRegistry newRegistry = TransactionRegistry.builder()
                            .transactionId(event.getTransactionId())
                            .status(TransactionStatus.COLLECTING_INTENTS)
                            .expectedParticipants(expectedParticipants)
                            .receivedResponses(0)
                            .build();
                    return registryRepository.save(newRegistry);
                });

        if (registry.getStatus() != TransactionStatus.COLLECTING_INTENTS) {
            log.info("Transaction {} already decided ({}), ignoring response",
                    event.getTransactionId(), registry.getStatus());
            return;
        }

        var existing = responseRepository.findByTransactionIdAndParticipantType(
                event.getTransactionId(), event.getParticipantType());
        if (existing.isPresent()) {
            log.info("Duplicate response from {} for transaction={}, ignoring",
                    event.getParticipantType(), event.getTransactionId());
            return;
        }

        ParticipantResponse response = ParticipantResponse.builder()
                .transactionId(event.getTransactionId())
                .participantType(event.getParticipantType())
                .status(event.getStatus())
                .reason(event.getReason())
                .build();
        responseRepository.save(response);

        registry.setReceivedResponses(registry.getReceivedResponses() + 1);

        if ("FAILED".equals(event.getStatus())) {
            makeDecision(registry, DecisionType.REJECT,
                    String.format("Participant %s failed: %s", event.getParticipantType(), event.getReason()));
        } else if (registry.getReceivedResponses().equals(registry.getExpectedParticipants())) {
            makeDecision(registry, DecisionType.COMMIT, null);
        } else {
            registryRepository.save(registry);
        }
    }

    @Transactional
    public void makeDecision(TransactionRegistry registry, DecisionType decision, String reason) {
        if (registry.getDecision() != null) {
            log.info("Transaction {} already has decision {}, skipping",
                    registry.getTransactionId(), registry.getDecision());
            return;
        }

        registry.setDecision(decision);
        registry.setDecisionReason(reason);
        registry.setStatus(decision == DecisionType.COMMIT ? TransactionStatus.COMMITTED : TransactionStatus.REJECTED);
        registry.setDecidedAt(LocalDateTime.now());
        registryRepository.save(registry);

        GlobalDecisionEvent event = GlobalDecisionEvent.builder()
                .transactionId(registry.getTransactionId())
                .decision(decision.name())
                .reason(reason)
                .build();

        decisionProducer.publishDecision(event);
        log.info("Decision {} published for transaction={}", decision, registry.getTransactionId());
    }
}
