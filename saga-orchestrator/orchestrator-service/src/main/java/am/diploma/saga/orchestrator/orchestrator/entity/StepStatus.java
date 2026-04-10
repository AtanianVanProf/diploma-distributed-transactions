package am.diploma.saga.orchestrator.orchestrator.entity;

public enum StepStatus {
    PENDING,
    COMPLETED,
    COMPENSATION_PENDING,
    COMPENSATED,
    COMPENSATION_FAILED
}
