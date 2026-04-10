package am.diploma.saga.orchestrator.orchestrator.controller;

import am.diploma.saga.orchestrator.orchestrator.dto.PlaceOrderRequest;
import am.diploma.saga.orchestrator.orchestrator.dto.SagaExecutionResponse;
import am.diploma.saga.orchestrator.orchestrator.dto.order.OrderResponse;
import am.diploma.saga.orchestrator.orchestrator.service.SagaOrchestrator;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orchestrator")
@RequiredArgsConstructor
public class OrchestratorController {

    private final SagaOrchestrator sagaOrchestrator;

    @PostMapping("/orders")
    public OrderResponse placeOrder(@RequestBody PlaceOrderRequest request) {
        return sagaOrchestrator.executeSaga(request);
    }

    @GetMapping("/sagas")
    public List<SagaExecutionResponse> getAllSagaExecutions() {
        return sagaOrchestrator.getAllSagaExecutions();
    }
}
