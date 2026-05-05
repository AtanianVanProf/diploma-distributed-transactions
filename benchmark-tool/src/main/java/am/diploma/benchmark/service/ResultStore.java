package am.diploma.benchmark.service;

import am.diploma.benchmark.dto.BenchmarkRunResponse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ResultStore {

    private final Map<String, BenchmarkRunResponse> runs = new ConcurrentHashMap<>();

    public void save(BenchmarkRunResponse run) {
        runs.put(run.runId(), run);
    }

    public BenchmarkRunResponse get(String runId) {
        return runs.get(runId);
    }

    public List<BenchmarkRunResponse> getAll() {
        return new ArrayList<>(runs.values());
    }
}
