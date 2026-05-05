package am.diploma.benchmark.controller;

import am.diploma.benchmark.dto.BenchmarkRunRequest;
import am.diploma.benchmark.dto.BenchmarkRunResponse;
import am.diploma.benchmark.service.BenchmarkRunner;
import am.diploma.benchmark.service.ResultStore;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/benchmark")
@RequiredArgsConstructor
public class BenchmarkController {

    private final BenchmarkRunner benchmarkRunner;
    private final ResultStore resultStore;

    @PostMapping("/run")
    public ResponseEntity<BenchmarkRunResponse> startRun(@RequestBody(required = false) BenchmarkRunRequest request) {
        if (request == null) {
            request = new BenchmarkRunRequest(null, null, null, null);
        }
        BenchmarkRunResponse response = benchmarkRunner.startRun(request);
        return ResponseEntity.accepted().body(response);
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, String>> getStatus() {
        return ResponseEntity.ok(Map.of("status", benchmarkRunner.getStatus()));
    }

    @GetMapping("/runs")
    public ResponseEntity<List<BenchmarkRunResponse>> getAllRuns() {
        return ResponseEntity.ok(resultStore.getAll());
    }

    @GetMapping("/runs/{runId}")
    public ResponseEntity<BenchmarkRunResponse> getRun(@PathVariable String runId) {
        BenchmarkRunResponse run = resultStore.get(runId);
        if (run == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(run);
    }

    @PostMapping("/reset")
    public ResponseEntity<Map<String, String>> resetAll() {
        benchmarkRunner.resetAll();
        return ResponseEntity.ok(Map.of("status", "reset_complete"));
    }
}
