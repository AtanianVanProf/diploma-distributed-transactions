package am.diploma.saga.choreography.order.controller;

import am.diploma.saga.choreography.order.dto.SagaExecutionResponse;
import am.diploma.saga.choreography.order.service.SagaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders/sagas")
@RequiredArgsConstructor
public class SagaController {

    private final SagaService sagaService;

    @GetMapping
    public List<SagaExecutionResponse> getAllSagaExecutions() {
        return sagaService.getAllSagaExecutions();
    }

    @GetMapping("/{sagaId}")
    public SagaExecutionResponse getSagaExecution(@PathVariable Long sagaId) {
        return sagaService.getSagaExecution(sagaId);
    }
}
