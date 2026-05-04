package am.diploma.pvip.protocol.order.controller;

import am.diploma.pvip.protocol.order.dto.ProtocolExecutionResponse;
import am.diploma.pvip.protocol.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders/protocol")
@RequiredArgsConstructor
public class ProtocolController {

    private final OrderService orderService;

    @GetMapping
    public List<ProtocolExecutionResponse> getAllProtocolExecutions() {
        return orderService.getAllProtocolExecutions();
    }

    @GetMapping("/{transactionId}")
    public ProtocolExecutionResponse getProtocolExecution(@PathVariable UUID transactionId) {
        return orderService.getProtocolExecution(transactionId);
    }
}
