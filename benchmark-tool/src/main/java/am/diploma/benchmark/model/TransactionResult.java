package am.diploma.benchmark.model;

public record TransactionResult(
        ProjectType project,
        ScenarioType scenario,
        long latencyMs,
        int kafkaMessages,
        int compensations,
        boolean success
) {}
