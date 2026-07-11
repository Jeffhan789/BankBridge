package io.bankbridge.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
public class SystemController {
    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of("status", "UP", "service", "bankbridge", "timestamp", Instant.now());
    }
}
