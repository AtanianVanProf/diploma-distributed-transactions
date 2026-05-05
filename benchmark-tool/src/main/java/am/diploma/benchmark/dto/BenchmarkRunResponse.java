package am.diploma.benchmark.dto;

import java.time.LocalDateTime;

public record BenchmarkRunResponse(
        String runId,
        String status,
        LocalDateTime startedAt,
        LocalDateTime completedAt,
        ComparisonData results
) {}
