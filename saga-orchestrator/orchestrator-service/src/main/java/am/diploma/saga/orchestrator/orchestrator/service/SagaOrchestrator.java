package am.diploma.saga.orchestrator.orchestrator.service;

import am.diploma.saga.orchestrator.orchestrator.dto.OrderItemRequest;
import am.diploma.saga.orchestrator.orchestrator.dto.PlaceOrderRequest;
import am.diploma.saga.orchestrator.orchestrator.dto.SagaExecutionResponse;
import am.diploma.saga.orchestrator.orchestrator.dto.SagaStepResponse;
import am.diploma.saga.orchestrator.orchestrator.dto.inventory.ReserveStockResponse;
import am.diploma.saga.orchestrator.orchestrator.dto.inventory.StockItemRequest;
import am.diploma.saga.orchestrator.orchestrator.dto.order.CreateOrderItemRequest;
import am.diploma.saga.orchestrator.orchestrator.dto.order.CreateOrderRequest;
import am.diploma.saga.orchestrator.orchestrator.dto.order.OrderResponse;
import am.diploma.saga.orchestrator.orchestrator.dto.payment.ChargeRequest;
import am.diploma.saga.orchestrator.orchestrator.dto.payment.ChargeResponse;
import am.diploma.saga.orchestrator.orchestrator.dto.payment.RefundRequest;
import am.diploma.saga.orchestrator.orchestrator.entity.SagaExecution;
import am.diploma.saga.orchestrator.orchestrator.entity.SagaStatus;
import am.diploma.saga.orchestrator.orchestrator.entity.SagaStep;
import am.diploma.saga.orchestrator.orchestrator.entity.StepStatus;
import am.diploma.saga.orchestrator.orchestrator.exception.ServiceCallException;
import am.diploma.saga.orchestrator.orchestrator.client.InventoryClient;
import am.diploma.saga.orchestrator.orchestrator.client.OrderClient;
import am.diploma.saga.orchestrator.orchestrator.client.PaymentClient;
import am.diploma.saga.orchestrator.orchestrator.repository.SagaExecutionRepository;
import am.diploma.saga.orchestrator.orchestrator.repository.SagaStepRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SagaOrchestrator {

    private final SagaExecutionRepository sagaExecutionRepository;
    private final SagaStepRepository sagaStepRepository;
    private final InventoryClient inventoryClient;
    private final PaymentClient paymentClient;
    private final OrderClient orderClient;
    private final ObjectMapper objectMapper;

    /**
     * Executes the 3-step order saga: reserve stock -> charge payment -> create order.
     * On failure, compensating transactions are triggered for any completed steps.
     */
    public OrderResponse executeSaga(PlaceOrderRequest request) {
        // 1. Create saga execution record
        SagaExecution saga = SagaExecution.builder()
                .status(SagaStatus.STARTED)
                .requestPayload(toJson(request))
                .build();
        saga = sagaExecutionRepository.save(saga);

        log.info("Saga {} started for customer {}", saga.getId(), request.getCustomerId());

        // 2. Step 1: Reserve stock
        SagaStep reserveStep = SagaStep.builder()
                .sagaExecution(saga)
                .stepName("RESERVE_STOCK")
                .stepOrder(1)
                .status(StepStatus.PENDING)
                .requestData(toJson(request.getItems()))
                .build();
        reserveStep = sagaStepRepository.save(reserveStep);
        saga.getSteps().add(reserveStep);

        ReserveStockResponse reserveStockResponse;
        try {
            reserveStockResponse = inventoryClient.reserveStock(request.getItems());
        } catch (ServiceCallException ex) {
            reserveStep.setStatus(StepStatus.FAILED);
            sagaStepRepository.save(reserveStep);

            saga.setStatus(SagaStatus.FAILED);
            saga.setFailureReason(ex.getErrorCode() + ": " + ex.getErrorMessage());
            sagaExecutionRepository.save(saga);

            log.warn("Step RESERVE_STOCK failed: {}", ex.getErrorCode());

            OrderResponse failedOrder = buildFailedOrder(
                    request.getCustomerId(),
                    saga.getFailureReason(),
                    request.getItems()
            );
            saga.setOrderId(failedOrder.getOrderId());
            sagaExecutionRepository.save(saga);

            return failedOrder;
        }

        reserveStep.setStatus(StepStatus.COMPLETED);
        reserveStep.setResponseData(toJson(reserveStockResponse));

        List<StockItemRequest> compensationItems = reserveStockResponse.getItems().stream()
                .map(item -> StockItemRequest.builder()
                        .productId(item.getProductId())
                        .quantity(item.getReservedQuantity())
                        .build())
                .toList();
        reserveStep.setCompensationData(toJson(compensationItems));
        reserveStep.setCompletedAt(LocalDateTime.now());
        sagaStepRepository.save(reserveStep);

        log.info("Step RESERVE_STOCK completed successfully");

        // 3. Step 2: Charge payment
        BigDecimal totalAmount = reserveStockResponse.getItems().stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getReservedQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        SagaStep chargeStep = SagaStep.builder()
                .sagaExecution(saga)
                .stepName("CHARGE_PAYMENT")
                .stepOrder(2)
                .status(StepStatus.PENDING)
                .requestData(toJson(new ChargeRequest(
                        request.getCustomerId(), totalAmount)))
                .build();
        chargeStep = sagaStepRepository.save(chargeStep);
        saga.getSteps().add(chargeStep);

        ChargeResponse chargeResponse;
        try {
            chargeResponse = paymentClient.charge(request.getCustomerId(), totalAmount);
        } catch (ServiceCallException ex) {
            chargeStep.setStatus(StepStatus.FAILED);
            sagaStepRepository.save(chargeStep);

            saga.setFailureReason(ex.getErrorCode() + ": " + ex.getErrorMessage());
            sagaExecutionRepository.save(saga);

            log.warn("Step CHARGE_PAYMENT failed: {}", ex.getErrorCode());

            compensate(saga);

            OrderResponse failedOrder = buildFailedOrder(
                    request.getCustomerId(),
                    saga.getFailureReason(),
                    reserveStockResponse
            );
            saga.setOrderId(failedOrder.getOrderId());
            sagaExecutionRepository.save(saga);

            return failedOrder;
        }

        chargeStep.setStatus(StepStatus.COMPLETED);
        chargeStep.setResponseData(toJson(chargeResponse));
        chargeStep.setCompensationData(toJson(new RefundRequest(
                request.getCustomerId(), totalAmount
        )));
        chargeStep.setCompletedAt(LocalDateTime.now());
        sagaStepRepository.save(chargeStep);

        log.info("Step CHARGE_PAYMENT completed successfully");

        // 4. Step 3: Create order
        SagaStep orderStep = SagaStep.builder()
                .sagaExecution(saga)
                .stepName("CREATE_ORDER")
                .stepOrder(3)
                .status(StepStatus.PENDING)
                .build();
        orderStep = sagaStepRepository.save(orderStep);
        saga.getSteps().add(orderStep);

        List<CreateOrderItemRequest> orderItems = reserveStockResponse.getItems().stream()
                .map(item -> CreateOrderItemRequest.builder()
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .quantity(item.getReservedQuantity())
                        .price(item.getPrice())
                        .build())
                .toList();

        CreateOrderRequest createOrderRequest = CreateOrderRequest.builder()
                .customerId(request.getCustomerId())
                .status("COMPLETED")
                .totalAmount(totalAmount)
                .failureReason(null)
                .items(orderItems)
                .build();

        orderStep.setRequestData(toJson(createOrderRequest));
        sagaStepRepository.save(orderStep);

        OrderResponse orderResponse;
        try {
            orderResponse = orderClient.createOrder(createOrderRequest);
        } catch (ServiceCallException ex) {
            orderStep.setStatus(StepStatus.FAILED);
            sagaStepRepository.save(orderStep);

            saga.setFailureReason(ex.getErrorCode() + ": " + ex.getErrorMessage());
            sagaExecutionRepository.save(saga);

            log.warn("Step CREATE_ORDER failed: {}", ex.getErrorCode());

            compensate(saga);

            OrderResponse failedOrder = buildFailedOrder(
                    request.getCustomerId(),
                    saga.getFailureReason(),
                    reserveStockResponse
            );
            saga.setOrderId(failedOrder.getOrderId());
            sagaExecutionRepository.save(saga);

            return failedOrder;
        }

        orderStep.setStatus(StepStatus.COMPLETED);
        orderStep.setResponseData(toJson(orderResponse));
        orderStep.setCompletedAt(LocalDateTime.now());
        sagaStepRepository.save(orderStep);

        saga.setStatus(SagaStatus.COMPLETED);
        saga.setOrderId(orderResponse.getOrderId());
        sagaExecutionRepository.save(saga);

        log.info("Saga completed. Order ID: {}", orderResponse.getOrderId());

        return orderResponse;
    }

    /**
     * Compensates all completed steps in reverse order.
     */
    private void compensate(SagaExecution saga) {
        saga.setStatus(SagaStatus.COMPENSATING);
        sagaExecutionRepository.save(saga);

        log.info("Compensation started for saga ID: {}", saga.getId());

        List<SagaStep> completedSteps = saga.getSteps().stream()
                .filter(step -> step.getStatus() == StepStatus.COMPLETED)
                .sorted(Comparator.comparing(SagaStep::getStepOrder).reversed())
                .toList();

        for (SagaStep step : completedSteps) {
            try {
                step.setStatus(StepStatus.COMPENSATION_PENDING);
                sagaStepRepository.save(step);

                executeCompensation(step);
                step.setStatus(StepStatus.COMPENSATED);
                sagaStepRepository.save(step);
                log.info("Step {} compensated successfully", step.getStepName());
            } catch (Exception e) {
                step.setStatus(StepStatus.COMPENSATION_FAILED);
                sagaStepRepository.save(step);
                saga.setStatus(SagaStatus.FAILED);
                saga.setFailureReason("Compensation failed for step: " + step.getStepName());
                sagaExecutionRepository.save(saga);
                log.error("CRITICAL: Compensation failed for step {}. Saga ID: {}, Step ID: {}",
                        step.getStepName(), saga.getId(), step.getId(), e);
                return;
            }
        }

        saga.setStatus(SagaStatus.COMPENSATED);
        sagaExecutionRepository.save(saga);
        log.info("Saga {} fully compensated", saga.getId());
    }

    /**
     * Dispatches compensation to the appropriate service based on step name.
     */
    private void executeCompensation(SagaStep step) throws JsonProcessingException {
        switch (step.getStepName()) {
            case "RESERVE_STOCK" -> {
                List<StockItemRequest> items = objectMapper.readValue(
                        step.getCompensationData(),
                        new TypeReference<>() {}
                );
                inventoryClient.releaseStock(items);
            }
            case "CHARGE_PAYMENT" -> {
                RefundRequest refundRequest =
                        objectMapper.readValue(step.getCompensationData(),
                                RefundRequest.class);
                paymentClient.refund(refundRequest.getCustomerId(), refundRequest.getAmount());
            }
            default -> log.warn("No compensation defined for step: {}", step.getStepName());
        }
    }

    /**
     * Returns all saga executions with their steps for the monitoring UI.
     */
    @Transactional(readOnly = true)
    public List<SagaExecutionResponse> getAllSagaExecutions() {
        return sagaExecutionRepository.findAllWithSteps().stream()
                .map(this::toSagaExecutionResponse)
                .toList();
    }

    private SagaExecutionResponse toSagaExecutionResponse(SagaExecution saga) {
        List<SagaStepResponse> stepResponses = saga.getSteps().stream()
                .sorted(Comparator.comparing(SagaStep::getStepOrder))
                .map(step -> SagaStepResponse.builder()
                        .id(step.getId())
                        .stepName(step.getStepName())
                        .stepOrder(step.getStepOrder())
                        .status(step.getStatus().name())
                        .requestData(step.getRequestData())
                        .responseData(step.getResponseData())
                        .compensationData(step.getCompensationData())
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

    /**
     * Builds a FAILED order via the order service when a saga step fails.
     * Uses PlaceOrderRequest items (before stock reservation is available).
     */
    private OrderResponse buildFailedOrder(Long customerId, String failureReason,
                                           List<OrderItemRequest> items) {
        List<CreateOrderItemRequest> orderItems = items.stream()
                .map(item -> CreateOrderItemRequest.builder()
                        .productId(item.getProductId())
                        .quantity(item.getQuantity())
                        .build())
                .toList();

        CreateOrderRequest createOrderRequest = CreateOrderRequest.builder()
                .customerId(customerId)
                .status("FAILED")
                .totalAmount(BigDecimal.ZERO)
                .failureReason(failureReason)
                .items(orderItems)
                .build();

        return orderClient.createOrder(createOrderRequest);
    }

    /**
     * Builds a FAILED order via the order service when a saga step fails.
     * Uses ReserveStockResponse items (when stock reservation data is available).
     */
    private OrderResponse buildFailedOrder(Long customerId, String failureReason,
                                           ReserveStockResponse reserveStockResponse) {
        List<CreateOrderItemRequest> orderItems = reserveStockResponse.getItems().stream()
                .map(item -> CreateOrderItemRequest.builder()
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .quantity(item.getReservedQuantity())
                        .price(item.getPrice())
                        .build())
                .toList();

        BigDecimal totalAmount = reserveStockResponse.getItems().stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getReservedQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        CreateOrderRequest createOrderRequest = CreateOrderRequest.builder()
                .customerId(customerId)
                .status("FAILED")
                .totalAmount(totalAmount)
                .failureReason(failureReason)
                .items(orderItems)
                .build();

        return orderClient.createOrder(createOrderRequest);
    }

    /**
     * Serializes an object to JSON string.
     */
    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize object to JSON", e);
        }
    }
}
