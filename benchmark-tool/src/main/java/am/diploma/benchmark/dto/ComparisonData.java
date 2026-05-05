package am.diploma.benchmark.dto;

import java.util.Map;

public record ComparisonData(
        Map<String, MetricsByProject> asyncMessages,
        Map<String, MetricsByProject> failureDetectionTime,
        Map<String, MetricsByProject> compensations,
        Map<String, MetricsByProject> dbWritesOnFailure
) {}
