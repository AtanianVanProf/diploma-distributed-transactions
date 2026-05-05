package am.diploma.benchmark.service;

import am.diploma.benchmark.adapter.ProjectAdapter;
import am.diploma.benchmark.config.BenchmarkProperties;
import am.diploma.benchmark.dto.BenchmarkRunRequest;
import am.diploma.benchmark.dto.BenchmarkRunResponse;
import am.diploma.benchmark.dto.ComparisonData;
import am.diploma.benchmark.dto.OrderItemRequest;
import am.diploma.benchmark.dto.PlaceOrderRequest;
import am.diploma.benchmark.model.ScenarioType;
import am.diploma.benchmark.model.TransactionResult;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@RequiredArgsConstructor
public class BenchmarkRunner {

    private static final Logger log = LoggerFactory.getLogger(BenchmarkRunner.class);

    private final List<ProjectAdapter> adapters;
    private final MetricsAggregator metricsAggregator;
    private final ResultStore resultStore;
    private final BenchmarkProperties properties;

    private volatile String currentRunId;
    private volatile String currentStatus = "IDLE";

    public BenchmarkRunResponse startRun(BenchmarkRunRequest request) {
        if ("RUNNING".equals(currentStatus)) {
            return resultStore.get(currentRunId);
        }

        String runId = UUID.randomUUID().toString();
        currentRunId = runId;
        currentStatus = "RUNNING";

        int happyCount = request.happyPathCount() != null ? request.happyPathCount() : properties.getScenarios().getHappyPathCount();
        int stockCount = request.stockFailureCount() != null ? request.stockFailureCount() : properties.getScenarios().getStockFailureCount();
        int balanceCount = request.balanceFailureCount() != null ? request.balanceFailureCount() : properties.getScenarios().getBalanceFailureCount();
        int timeoutCount = request.timeoutCount() != null ? request.timeoutCount() : properties.getScenarios().getTimeoutCount();

        BenchmarkRunResponse initial = new BenchmarkRunResponse(runId, "RUNNING", LocalDateTime.now(), null, null);
        resultStore.save(initial);

        CompletableFuture.runAsync(() -> executeRun(runId, happyCount, stockCount, balanceCount, timeoutCount));

        return initial;
    }

    public String getStatus() {
        return currentStatus;
    }

    public void resetAll() {
        for (ProjectAdapter adapter : adapters) {
            try {
                adapter.reset();
            } catch (Exception e) {
                log.warn("Failed to reset {}: {}", adapter.getProjectType(), e.getMessage());
            }
        }
    }

    private void executeRun(String runId, int happyCount, int stockCount, int balanceCount, int timeoutCount) {
        List<TransactionResult> allResults = new CopyOnWriteArrayList<>();

        try {
            log.info("Starting benchmark run {} with {} happy, {} stock fail, {} balance fail, {} timeout",
                    runId, happyCount, stockCount, balanceCount, timeoutCount);

            List<CompletableFuture<List<TransactionResult>>> futures = adapters.stream()
                    .map(adapter -> CompletableFuture.supplyAsync(() ->
                            executeForAdapter(adapter, happyCount, stockCount, balanceCount, timeoutCount)))
                    .toList();

            futures.forEach(f -> allResults.addAll(f.join()));

            ComparisonData comparison = metricsAggregator.aggregate(allResults);
            BenchmarkRunResponse completed = new BenchmarkRunResponse(
                    runId, "COMPLETED", resultStore.get(runId).startedAt(), LocalDateTime.now(), comparison);
            resultStore.save(completed);
            currentStatus = "COMPLETED";

            log.info("Benchmark run {} completed with {} total results", runId, allResults.size());
        } catch (Exception e) {
            log.error("Benchmark run {} failed: {}", runId, e.getMessage(), e);
            BenchmarkRunResponse failed = new BenchmarkRunResponse(
                    runId, "FAILED", resultStore.get(runId).startedAt(), LocalDateTime.now(), null);
            resultStore.save(failed);
            currentStatus = "FAILED";
        }
    }

    private List<TransactionResult> executeForAdapter(ProjectAdapter adapter, int happyCount, int stockCount, int balanceCount, int timeoutCount) {
        List<TransactionResult> results = new ArrayList<>();
        int batchSize = properties.getScenarios().getResetBatchSize();

        results.addAll(executeBatched(adapter, ScenarioType.HAPPY_PATH, happyPathRequest(), happyCount, batchSize));
        results.addAll(executeScenario(adapter, ScenarioType.STOCK_FAILURE, stockFailureRequest(), stockCount));
        results.addAll(executeScenario(adapter, ScenarioType.BALANCE_FAILURE, balanceFailureRequest(), balanceCount));
        results.addAll(executeScenario(adapter, ScenarioType.TIMEOUT, happyPathRequest(), timeoutCount));

        return results;
    }

    private List<TransactionResult> executeBatched(ProjectAdapter adapter, ScenarioType scenario,
                                                    PlaceOrderRequest request, int totalCount, int batchSize) {
        List<TransactionResult> results = new ArrayList<>();
        int remaining = totalCount;

        while (remaining > 0) {
            int batch = Math.min(remaining, batchSize);
            for (int i = 0; i < batch; i++) {
                TransactionResult result = adapter.executeOrder(request, scenario);
                results.add(result);
            }
            remaining -= batch;
            if (remaining > 0) {
                adapter.reset();
                sleep(500);
            }
        }

        return results;
    }

    private List<TransactionResult> executeScenario(ProjectAdapter adapter, ScenarioType scenario,
                                                     PlaceOrderRequest request, int count) {
        List<TransactionResult> results = new ArrayList<>();
        adapter.reset();
        sleep(500);

        for (int i = 0; i < count; i++) {
            TransactionResult result = adapter.executeOrder(request, scenario);
            results.add(result);
        }

        return results;
    }

    private PlaceOrderRequest happyPathRequest() {
        return new PlaceOrderRequest(1L, List.of(new OrderItemRequest(2L, 1)));
    }

    private PlaceOrderRequest stockFailureRequest() {
        return new PlaceOrderRequest(1L, List.of(new OrderItemRequest(4L, 1)));
    }

    private PlaceOrderRequest balanceFailureRequest() {
        return new PlaceOrderRequest(2L, List.of(new OrderItemRequest(1L, 1)));
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
