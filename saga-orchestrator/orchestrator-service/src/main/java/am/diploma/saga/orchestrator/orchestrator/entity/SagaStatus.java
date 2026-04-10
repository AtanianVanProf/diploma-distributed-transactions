package am.diploma.saga.orchestrator.orchestrator.entity;

public enum SagaStatus {
    STARTED,
    COMPLETING,
    COMPLETED,
    COMPENSATING,
    COMPENSATED,
    FAILED
}
