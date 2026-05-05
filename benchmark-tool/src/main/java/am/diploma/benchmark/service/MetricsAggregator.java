package am.diploma.benchmark.service;

import am.diploma.benchmark.dto.ComparisonData;
import am.diploma.benchmark.dto.MetricsByProject;
import am.diploma.benchmark.model.ProjectType;
import am.diploma.benchmark.model.ScenarioType;
import am.diploma.benchmark.model.TransactionResult;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class MetricsAggregator {

    private static final Map<ScenarioType, Map<ProjectType, Integer>> DB_WRITES_ON_FAILURE = Map.of(
            ScenarioType.STOCK_FAILURE, Map.of(
                    ProjectType.ORCHESTRATOR, 7,
                    ProjectType.CHOREOGRAPHY, 4,
                    ProjectType.PVIP, 2
            ),
            ScenarioType.BALANCE_FAILURE, Map.of(
                    ProjectType.ORCHESTRATOR, 15,
                    ProjectType.CHOREOGRAPHY, 7,
                    ProjectType.PVIP, 2
            )
    );

    public ComparisonData aggregate(List<TransactionResult> results) {
        Map<ScenarioType, Map<ProjectType, List<TransactionResult>>> grouped = results.stream()
                .collect(Collectors.groupingBy(
                        TransactionResult::scenario,
                        () -> new EnumMap<>(ScenarioType.class),
                        Collectors.groupingBy(
                                TransactionResult::project,
                                () -> new EnumMap<>(ProjectType.class),
                                Collectors.toList()
                        )
                ));

        Map<String, MetricsByProject> asyncMessages = buildAsyncMessages(grouped);
        Map<String, MetricsByProject> failureDetectionTime = buildFailureDetectionTime(grouped);
        Map<String, MetricsByProject> compensations = buildCompensations(grouped);
        Map<String, MetricsByProject> dbWritesOnFailure = buildDbWritesOnFailure(grouped);

        return new ComparisonData(asyncMessages, failureDetectionTime, compensations, dbWritesOnFailure);
    }

    private Map<String, MetricsByProject> buildAsyncMessages(
            Map<ScenarioType, Map<ProjectType, List<TransactionResult>>> grouped) {
        Map<String, MetricsByProject> result = new LinkedHashMap<>();

        for (ScenarioType scenario : List.of(ScenarioType.HAPPY_PATH, ScenarioType.STOCK_FAILURE, ScenarioType.BALANCE_FAILURE)) {
            Map<ProjectType, List<TransactionResult>> byProject = grouped.getOrDefault(scenario, Map.of());
            if (byProject.isEmpty()) continue;

            result.put(scenario.name().toLowerCase(), new MetricsByProject(
                    0,
                    avgKafka(byProject.get(ProjectType.CHOREOGRAPHY)),
                    avgKafka(byProject.get(ProjectType.PVIP))
            ));
        }

        return result;
    }

    private Map<String, MetricsByProject> buildFailureDetectionTime(
            Map<ScenarioType, Map<ProjectType, List<TransactionResult>>> grouped) {
        Map<String, MetricsByProject> result = new LinkedHashMap<>();

        for (ScenarioType scenario : List.of(ScenarioType.STOCK_FAILURE, ScenarioType.BALANCE_FAILURE)) {
            Map<ProjectType, List<TransactionResult>> byProject = grouped.getOrDefault(scenario, Map.of());
            if (byProject.isEmpty()) continue;

            result.put(scenario.name().toLowerCase(), new MetricsByProject(
                    avgLatency(byProject.get(ProjectType.ORCHESTRATOR)),
                    avgLatency(byProject.get(ProjectType.CHOREOGRAPHY)),
                    avgLatency(byProject.get(ProjectType.PVIP))
            ));
        }

        return result;
    }

    private Map<String, MetricsByProject> buildCompensations(
            Map<ScenarioType, Map<ProjectType, List<TransactionResult>>> grouped) {
        Map<String, MetricsByProject> result = new LinkedHashMap<>();

        for (ScenarioType scenario : List.of(ScenarioType.STOCK_FAILURE, ScenarioType.BALANCE_FAILURE)) {
            Map<ProjectType, List<TransactionResult>> byProject = grouped.getOrDefault(scenario, Map.of());
            if (byProject.isEmpty()) continue;

            result.put(scenario.name().toLowerCase(), new MetricsByProject(
                    avgCompensations(byProject.get(ProjectType.ORCHESTRATOR)),
                    avgCompensations(byProject.get(ProjectType.CHOREOGRAPHY)),
                    avgCompensations(byProject.get(ProjectType.PVIP))
            ));
        }

        return result;
    }

    private Map<String, MetricsByProject> buildDbWritesOnFailure(
            Map<ScenarioType, Map<ProjectType, List<TransactionResult>>> grouped) {
        Map<String, MetricsByProject> result = new LinkedHashMap<>();

        for (ScenarioType scenario : List.of(ScenarioType.STOCK_FAILURE, ScenarioType.BALANCE_FAILURE)) {
            Map<ProjectType, List<TransactionResult>> byProject = grouped.getOrDefault(scenario, Map.of());
            if (byProject.isEmpty()) continue;

            Map<ProjectType, Integer> writes = DB_WRITES_ON_FAILURE.getOrDefault(scenario, Map.of());
            result.put(scenario.name().toLowerCase(), new MetricsByProject(
                    writes.getOrDefault(ProjectType.ORCHESTRATOR, 0),
                    writes.getOrDefault(ProjectType.CHOREOGRAPHY, 0),
                    writes.getOrDefault(ProjectType.PVIP, 0)
            ));
        }

        return result;
    }

    private double avgKafka(List<TransactionResult> results) {
        if (results == null || results.isEmpty()) return 0;
        return results.stream().mapToInt(TransactionResult::kafkaMessages).average().orElse(0);
    }

    private double avgLatency(List<TransactionResult> results) {
        if (results == null || results.isEmpty()) return 0;
        return results.stream().mapToLong(TransactionResult::latencyMs).average().orElse(0);
    }

    private double avgCompensations(List<TransactionResult> results) {
        if (results == null || results.isEmpty()) return 0;
        return results.stream().mapToInt(TransactionResult::compensations).average().orElse(0);
    }
}
