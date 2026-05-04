package am.diploma.pvip.protocol.order.controller;

import am.diploma.pvip.protocol.order.service.DemoService;
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
        demoService.resetAll();
        return Map.of("message", "All services reset to initial state");
    }
}
