package am.diploma.pvip.protocol.commitgate.service;

import am.diploma.pvip.protocol.commitgate.entity.DecisionType;
import am.diploma.pvip.protocol.commitgate.entity.TransactionRegistry;
import am.diploma.pvip.protocol.commitgate.entity.TransactionStatus;
import am.diploma.pvip.protocol.commitgate.repository.TransactionRegistryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class TimeoutService {

    private final TransactionRegistryRepository registryRepository;
    private final CommitGateService commitGateService;

    @Value("${pvip.timeout-ms}")
    private long timeoutMs;

    @Scheduled(fixedRate = 1000)
    @Transactional
    public void checkTimeouts() {
        LocalDateTime cutoff = LocalDateTime.now().minusNanos(timeoutMs * 1_000_000);
        List<TransactionRegistry> timedOut = registryRepository.findByStatusAndCreatedAtBefore(
                TransactionStatus.COLLECTING_INTENTS, cutoff);

        for (TransactionRegistry registry : timedOut) {
            TransactionRegistry locked = registryRepository.findByTransactionIdForUpdate(registry.getTransactionId())
                    .orElse(null);
            if (locked == null || locked.getDecision() != null) {
                continue;
            }
            log.warn("Transaction {} timed out after {}ms", locked.getTransactionId(), timeoutMs);
            commitGateService.makeDecision(locked, DecisionType.REJECT,
                    String.format("Timeout: not all participants responded within %dms", timeoutMs));
        }
    }
}
