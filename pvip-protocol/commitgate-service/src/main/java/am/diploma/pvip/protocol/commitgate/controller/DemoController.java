package am.diploma.pvip.protocol.commitgate.controller;

import am.diploma.pvip.protocol.commitgate.service.DemoService;
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
    public Map<String, String> reset() {
        demoService.resetData();
        return Map.of("message", "Commit Gate database reset to initial state");
    }
}
