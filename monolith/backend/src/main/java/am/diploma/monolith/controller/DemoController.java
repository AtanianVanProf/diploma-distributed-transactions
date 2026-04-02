package am.diploma.monolith.controller;

import am.diploma.monolith.service.DemoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/demo")
@RequiredArgsConstructor
public class DemoController {

    private final DemoService demoService;

    @PostMapping("/reset")
    public Map<String, String> resetData() {
        demoService.resetData();
        return Map.of("message", "Database reset to initial state");
    }
}
