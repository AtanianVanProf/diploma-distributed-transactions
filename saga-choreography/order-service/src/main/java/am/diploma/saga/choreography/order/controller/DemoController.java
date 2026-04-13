package am.diploma.saga.choreography.order.controller;

import am.diploma.saga.choreography.order.service.DemoService;
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
        demoService.resetAllData();
        return Map.of("message", "All databases reset to initial state");
    }
}
