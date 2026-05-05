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
public class OrchestratorAdapter implements ProjectAdapter {

    private final RestTemplate restTemplate;
    private final BenchmarkProperties properties;

    @Override
    public ProjectType getProjectType() {
        return ProjectType.ORCHESTRATOR;
    }

    @Override
    public TransactionResult executeOrder(PlaceOrderRequest request, ScenarioType scenario) {
        String url = properties.getTargets().getOrchestrator().getBaseUrl()
                + properties.getTargets().getOrchestrator().getPlaceOrderPath();

        long start = System.currentTimeMillis();

        try {
            Map<String, Object> response = restTemplate.postForObject(url, request, Map.class);
            long latency = System.currentTimeMillis() - start;

            boolean success = response != null && "COMPLETED".equals(response.get("status"));
            int compensations = countCompensations();

            return new TransactionResult(ProjectType.ORCHESTRATOR, scenario, latency, 0, compensations, success);
        } catch (Exception e) {
            long latency = System.currentTimeMillis() - start;
            return new TransactionResult(ProjectType.ORCHESTRATOR, scenario, latency, 0, 0, false);
        }
    }

    @Override
    public void reset() {
        String url = properties.getTargets().getOrchestrator().getBaseUrl()
                + properties.getTargets().getOrchestrator().getResetPath();
        restTemplate.postForObject(url, null, Void.class);
    }

    @SuppressWarnings("unchecked")
    private int countCompensations() {
        String url = properties.getTargets().getOrchestrator().getBaseUrl()
                + properties.getTargets().getOrchestrator().getSagasPath();

        try {
            List<Map<String, Object>> sagas = restTemplate.getForObject(url, List.class);
            if (sagas == null || sagas.isEmpty()) return 0;

            Map<String, Object> lastSaga = sagas.getFirst();
            List<Map<String, Object>> steps = (List<Map<String, Object>>) lastSaga.get("steps");
            if (steps == null) return 0;

            return (int) steps.stream()
                    .filter(step -> "COMPENSATED".equals(step.get("status")))
                    .count();
        } catch (Exception e) {
            return 0;
        }
    }
}
