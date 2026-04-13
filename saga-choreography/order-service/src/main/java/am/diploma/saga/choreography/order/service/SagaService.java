package am.diploma.saga.choreography.order.service;

import am.diploma.saga.choreography.order.dto.SagaExecutionResponse;
import am.diploma.saga.choreography.order.dto.SagaStepResponse;
import am.diploma.saga.choreography.order.entity.SagaExecution;
import am.diploma.saga.choreography.order.exception.NotFoundException;
import am.diploma.saga.choreography.order.repository.SagaExecutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SagaService {

    private final SagaExecutionRepository sagaExecutionRepository;

    @Transactional(readOnly = true)
    public List<SagaExecutionResponse> getAllSagaExecutions() {
        return sagaExecutionRepository.findAllWithSteps().stream()
                .map(this::toSagaExecutionResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SagaExecutionResponse getSagaExecution(Long sagaId) {
        SagaExecution saga = sagaExecutionRepository.findByIdWithSteps(sagaId)
                .orElseThrow(() -> new NotFoundException("SAGA_NOT_FOUND",
                        "Saga execution with ID " + sagaId + " not found"));
        return toSagaExecutionResponse(saga);
    }

    private SagaExecutionResponse toSagaExecutionResponse(SagaExecution saga) {
        List<SagaStepResponse> stepResponses = saga.getSteps().stream()
                .map(step -> SagaStepResponse.builder()
                        .id(step.getId())
                        .stepName(step.getStepName())
                        .stepOrder(step.getStepOrder())
                        .status(step.getStatus().name())
                        .completedAt(step.getCompletedAt())
                        .build())
                .toList();

        return SagaExecutionResponse.builder()
                .id(saga.getId())
                .orderId(saga.getOrderId())
                .status(saga.getStatus().name())
                .requestPayload(saga.getRequestPayload())
                .failureReason(saga.getFailureReason())
                .steps(stepResponses)
                .createdAt(saga.getCreatedAt())
                .updatedAt(saga.getUpdatedAt())
                .build();
    }
}
