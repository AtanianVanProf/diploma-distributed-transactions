package am.diploma.saga.choreography.order.service;

import am.diploma.saga.choreography.order.dto.SagaExecutionResponse;
import am.diploma.saga.choreography.order.dto.SagaStepResponse;
import am.diploma.saga.choreography.order.entity.*;
import am.diploma.saga.choreography.order.event.OrderCompletedEvent;
import am.diploma.saga.choreography.order.event.OrderFailedEvent;
import am.diploma.saga.choreography.order.event.StockReservationFailedEvent;
import am.diploma.saga.choreography.order.event.StockReservedEvent;
import am.diploma.saga.choreography.order.exception.NotFoundException;
import am.diploma.saga.choreography.order.repository.OrderRepository;
import am.diploma.saga.choreography.order.repository.SagaExecutionRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SagaService {

    private static final Logger log = LoggerFactory.getLogger(SagaService.class);

    private final SagaExecutionRepository sagaExecutionRepository;
    private final OrderRepository orderRepository;

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

    @Transactional
    public void handleStockReserved(StockReservedEvent event) {
        SagaExecution saga = sagaExecutionRepository.findByIdWithSteps(event.getSagaId())
                .orElseThrow(() -> new NotFoundException("SAGA_NOT_FOUND",
                        "Saga execution with ID " + event.getSagaId() + " not found"));

        saga.getSteps().stream()
                .filter(step -> "RESERVE_STOCK".equals(step.getStepName()))
                .findFirst()
                .ifPresent(step -> {
                    step.setStatus(StepStatus.COMPLETED);
                    step.setCompletedAt(LocalDateTime.now());
                });

        sagaExecutionRepository.save(saga);
        log.info("Saga {} step RESERVE_STOCK completed", event.getSagaId());
    }

    @Transactional
    public void handleStockReservationFailed(StockReservationFailedEvent event) {
        SagaExecution saga = sagaExecutionRepository.findByIdWithSteps(event.getSagaId())
                .orElseThrow(() -> new NotFoundException("SAGA_NOT_FOUND",
                        "Saga execution with ID " + event.getSagaId() + " not found"));

        saga.getSteps().stream()
                .filter(step -> "RESERVE_STOCK".equals(step.getStepName()))
                .findFirst()
                .ifPresent(step -> {
                    step.setStatus(StepStatus.FAILED);
                    step.setCompletedAt(LocalDateTime.now());
                });

        saga.setStatus(SagaStatus.FAILED);
        saga.setFailureReason(event.getReason());
        sagaExecutionRepository.save(saga);

        Order order = orderRepository.findById(saga.getOrderId())
                .orElseThrow(() -> new NotFoundException("ORDER_NOT_FOUND",
                        "Order with ID " + saga.getOrderId() + " not found"));
        order.setStatus(OrderStatus.FAILED);
        order.setFailureReason(event.getReason());
        orderRepository.save(order);

        log.info("Saga {} failed at RESERVE_STOCK: {}", event.getSagaId(), event.getReason());
    }

    @Transactional
    public void handleOrderCompleted(OrderCompletedEvent event) {
        SagaExecution saga = sagaExecutionRepository.findByIdWithSteps(event.getSagaId())
                .orElseThrow(() -> new NotFoundException("SAGA_NOT_FOUND",
                        "Saga execution with ID " + event.getSagaId() + " not found"));

        LocalDateTime now = LocalDateTime.now();
        saga.getSteps().stream()
                .filter(step -> step.getStatus() != StepStatus.COMPLETED)
                .forEach(step -> {
                    step.setStatus(StepStatus.COMPLETED);
                    step.setCompletedAt(now);
                });

        saga.setStatus(SagaStatus.COMPLETED);
        sagaExecutionRepository.save(saga);

        Order order = orderRepository.findByIdWithItems(saga.getOrderId())
                .orElseThrow(() -> new NotFoundException("ORDER_NOT_FOUND",
                        "Order with ID " + saga.getOrderId() + " not found"));
        order.setStatus(OrderStatus.COMPLETED);
        order.setTotalAmount(event.getTotalAmount());

        for (OrderCompletedEvent.CompletedItem completedItem : event.getItems()) {
            order.getItems().stream()
                    .filter(item -> item.getProductId().equals(completedItem.getProductId()))
                    .findFirst()
                    .ifPresent(item -> {
                        item.setProductName(completedItem.getProductName());
                        item.setPriceAtPurchase(completedItem.getPrice());
                    });
        }

        orderRepository.save(order);
        log.info("Saga {} completed, order {} fulfilled", event.getSagaId(), event.getOrderId());
    }

    @Transactional
    public void handleOrderFailed(OrderFailedEvent event) {
        SagaExecution saga = sagaExecutionRepository.findByIdWithSteps(event.getSagaId())
                .orElseThrow(() -> new NotFoundException("SAGA_NOT_FOUND",
                        "Saga execution with ID " + event.getSagaId() + " not found"));

        saga.getSteps().stream()
                .filter(step -> "RESERVE_STOCK".equals(step.getStepName()))
                .findFirst()
                .ifPresent(step -> {
                    step.setStatus(StepStatus.COMPENSATED);
                    step.setCompletedAt(LocalDateTime.now());
                });

        saga.getSteps().stream()
                .filter(step -> "CHARGE_PAYMENT".equals(step.getStepName()))
                .findFirst()
                .ifPresent(step -> {
                    step.setStatus(StepStatus.FAILED);
                    step.setCompletedAt(LocalDateTime.now());
                });

        saga.setStatus(SagaStatus.COMPENSATED);
        saga.setFailureReason(event.getReason());
        sagaExecutionRepository.save(saga);

        Order order = orderRepository.findById(saga.getOrderId())
                .orElseThrow(() -> new NotFoundException("ORDER_NOT_FOUND",
                        "Order with ID " + saga.getOrderId() + " not found"));
        order.setStatus(OrderStatus.FAILED);
        order.setFailureReason(event.getReason());
        orderRepository.save(order);

        log.info("Saga {} compensated (payment failed), order {} failed: {}", event.getSagaId(), event.getOrderId(), event.getReason());
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
