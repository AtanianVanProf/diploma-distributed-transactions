package am.diploma.pvip.protocol.inventory.controller;

import am.diploma.pvip.protocol.inventory.kafka.IntentRequestConsumer;
import am.diploma.pvip.protocol.inventory.service.DemoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/demo")
@RequiredArgsConstructor
public class DemoController {

    private final DemoService demoService;
    private final IntentRequestConsumer intentRequestConsumer;

    @PostMapping("/reset")
    public Map<String, String> resetData() {
        demoService.resetData();
        intentRequestConsumer.resume();
        return Map.of("message", "Inventory database reset to initial state");
    }

    @PostMapping("/pause-kafka")
    public Map<String, String> pauseKafka() {
        intentRequestConsumer.pause();
        return Map.of("message", "Kafka consumer paused");
    }

    @PostMapping("/resume-kafka")
    public Map<String, String> resumeKafka() {
        intentRequestConsumer.resume();
        return Map.of("message", "Kafka consumer resumed");
    }

    @GetMapping("/kafka-status")
    public Map<String, Object> kafkaStatus() {
        return Map.of("paused", intentRequestConsumer.isPaused());
    }
}
