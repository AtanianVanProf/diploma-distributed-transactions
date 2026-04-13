package am.diploma.saga.choreography.payment.controller;

import am.diploma.saga.choreography.payment.service.DemoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/demo")
@RequiredArgsConstructor
public class DemoController {

    private final DemoService demoService;

    @PostMapping("/reset")
    public Map<String, String> resetData() {
        demoService.resetData();
        return Map.of("message", "Payment database reset to initial state");
    }
}
