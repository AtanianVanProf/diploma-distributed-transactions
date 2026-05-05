package am.diploma.benchmark.adapter;

import am.diploma.benchmark.config.BenchmarkProperties;
import am.diploma.benchmark.dto.PlaceOrderRequest;
import am.diploma.benchmark.model.ProjectType;
import am.diploma.benchmark.model.ScenarioType;
import am.diploma.benchmark.model.TransactionResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class PvipAdapter implements ProjectAdapter {

    private static final int KAFKA_MESSAGES_HAPPY_PATH = 5;
    private static final int KAFKA_MESSAGES_PRE_VALIDATION_FAIL = 0;

    private final RestTemplate restTemplate;
    private final BenchmarkProperties properties;

    @Override
    public ProjectType getProjectType() {
        return ProjectType.PVIP;
    }

    @Override
    @SuppressWarnings("unchecked")
    public TransactionResult executeOrder(PlaceOrderRequest request, ScenarioType scenario) {
        String baseUrl = properties.getTargets().getPvip().getBaseUrl();
        String placeUrl = baseUrl + properties.getTargets().getPvip().getPlaceOrderPath();

        long start = System.currentTimeMillis();

        try {
            ResponseEntity<Map> responseEntity = restTemplate.postForEntity(placeUrl, request, Map.class);
            Map<String, Object> placeResponse = responseEntity.getBody();

            if (responseEntity.getStatusCode() == HttpStatus.CONFLICT || placeResponse == null) {
                long latency = System.currentTimeMillis() - start;
                return new TransactionResult(ProjectType.PVIP, scenario, latency, KAFKA_MESSAGES_PRE_VALIDATION_FAIL, 0, false);
            }

            Object transactionId = placeResponse.get("transactionId");
            if (transactionId == null) {
                long latency = System.currentTimeMillis() - start;
                return new TransactionResult(ProjectType.PVIP, scenario, latency, 0, 0, false);
            }

            String pollUrl = baseUrl + properties.getTargets().getPvip().getProtocolPath()
                    .replace("{transactionId}", transactionId.toString());

            Map<String, Object> protocolResult = pollUntilTerminal(pollUrl);
            long latency = System.currentTimeMillis() - start;

            String status = (String) protocolResult.get("status");
            boolean success = "COMMITTED".equals(status);
            int kafkaMessages = success ? KAFKA_MESSAGES_HAPPY_PATH : KAFKA_MESSAGES_PRE_VALIDATION_FAIL;
            int compensations = toInt(protocolResult.get("compensationsTriggered"));

            return new TransactionResult(ProjectType.PVIP, scenario, latency, kafkaMessages, compensations, success);
        } catch (HttpClientErrorException.Conflict e) {
            long latency = System.currentTimeMillis() - start;
            return new TransactionResult(ProjectType.PVIP, scenario, latency, 0, 0, false);
        } catch (Exception e) {
            long latency = System.currentTimeMillis() - start;
            return new TransactionResult(ProjectType.PVIP, scenario, latency, 0, 0, false);
        }
    }

    @Override
    public void reset() {
        String url = properties.getTargets().getPvip().getBaseUrl()
                + properties.getTargets().getPvip().getResetPath();
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
                if ("COMMITTED".equals(status) || "REJECTED".equals(status)) {
                    return response;
                }
            }
            delay = Math.min(delay * 2, maxDelay);
        }

        return Map.of("status", "TIMEOUT", "kafkaMessagesSent", 0, "compensationsTriggered", 0);
    }

    private int toInt(Object value) {
        if (value == null) return 0;
        if (value instanceof Number n) return n.intValue();
        return 0;
    }
}
