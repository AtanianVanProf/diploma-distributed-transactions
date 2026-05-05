package am.diploma.benchmark.adapter;

import am.diploma.benchmark.config.BenchmarkProperties;
import am.diploma.benchmark.dto.PlaceOrderRequest;
import am.diploma.benchmark.model.ProjectType;
import am.diploma.benchmark.model.ScenarioType;
import am.diploma.benchmark.model.TransactionResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ChoreographyAdapter implements ProjectAdapter {

    private static final Map<ScenarioType, Integer> KAFKA_MESSAGES = Map.of(
            ScenarioType.HAPPY_PATH, 4,
            ScenarioType.STOCK_FAILURE, 2,
            ScenarioType.BALANCE_FAILURE, 4,
            ScenarioType.TIMEOUT, 0
    );

    private final RestTemplate restTemplate;
    private final BenchmarkProperties properties;

    @Override
    public ProjectType getProjectType() {
        return ProjectType.CHOREOGRAPHY;
    }

    @Override
    @SuppressWarnings("unchecked")
    public TransactionResult executeOrder(PlaceOrderRequest request, ScenarioType scenario) {
        String baseUrl = properties.getTargets().getChoreography().getBaseUrl();
        String placeUrl = baseUrl + properties.getTargets().getChoreography().getPlaceOrderPath();

        long start = System.currentTimeMillis();

        try {
            Map<String, Object> placeResponse = restTemplate.postForObject(placeUrl, request, Map.class);
            if (placeResponse == null) {
                long latency = System.currentTimeMillis() - start;
                return new TransactionResult(ProjectType.CHOREOGRAPHY, scenario, latency, 0, 0, false);
            }

            Number sagaId = (Number) placeResponse.get("sagaId");
            String pollUrl = baseUrl + properties.getTargets().getChoreography().getSagaPath()
                    .replace("{sagaId}", sagaId.toString());

            Map<String, Object> sagaResult = pollUntilTerminal(pollUrl);
            long latency = System.currentTimeMillis() - start;

            String status = (String) sagaResult.get("status");
            boolean success = "COMPLETED".equals(status);
            int compensations = countCompensationsFromSaga(sagaResult);
            int kafkaMessages = KAFKA_MESSAGES.getOrDefault(scenario, 0);

            return new TransactionResult(ProjectType.CHOREOGRAPHY, scenario, latency, kafkaMessages, compensations, success);
        } catch (Exception e) {
            long latency = System.currentTimeMillis() - start;
            return new TransactionResult(ProjectType.CHOREOGRAPHY, scenario, latency, 0, 0, false);
        }
    }

    @Override
    public void reset() {
        String url = properties.getTargets().getChoreography().getBaseUrl()
                + properties.getTargets().getChoreography().getResetPath();
        restTemplate.postForObject(url, null, Void.class);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> pollUntilTerminal(String url) throws InterruptedException {
        long timeout = properties.getPolling().getTimeoutMs();
        long delay = properties.getPolling().getInitialDelayMs();
        long maxDelay = properties.getPolling().getMaxDelayMs();
        long deadline = System.currentTimeMillis() + timeout;

        while (System.currentTimeMillis() < deadline) {
            Thread.sleep(delay);
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response != null) {
                String status = (String) response.get("status");
                if (!"STARTED".equals(status)) {
                    return response;
                }
            }
            delay = Math.min(delay * 2, maxDelay);
        }

        return Map.of("status", "TIMEOUT");
    }

    @SuppressWarnings("unchecked")
    private int countCompensationsFromSaga(Map<String, Object> saga) {
        List<Map<String, Object>> steps = (List<Map<String, Object>>) saga.get("steps");
        if (steps == null) return 0;

        return (int) steps.stream()
                .filter(step -> "COMPENSATED".equals(step.get("status")))
                .count();
    }
}
