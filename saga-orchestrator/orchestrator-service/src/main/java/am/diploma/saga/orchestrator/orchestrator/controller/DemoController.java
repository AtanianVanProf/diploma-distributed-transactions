package am.diploma.saga.orchestrator.orchestrator.controller;

import am.diploma.saga.orchestrator.orchestrator.service.DemoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/orchestrator")
@RequiredArgsConstructor
public class DemoController {

    private final DemoService demoService;

    @PostMapping("/reset")
    public Map<String, String> resetAllData() {
        demoService.resetAllData();
        return Map.of("message", "All databases reset to initial state");
    }
}
