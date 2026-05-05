package am.diploma.benchmark.dto;

public record BenchmarkRunRequest(
        Integer happyPathCount,
        Integer stockFailureCount,
        Integer balanceFailureCount,
        Integer timeoutCount
) {}
