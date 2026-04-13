package am.diploma.saga.choreography.order.entity;

public enum SagaStatus {
    STARTED,
    COMPLETED,
    COMPENSATING,
    COMPENSATED,
    FAILED
}
