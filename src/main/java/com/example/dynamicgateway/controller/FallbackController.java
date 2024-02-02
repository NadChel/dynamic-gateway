package com.example.dynamicgateway.controller;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@Slf4j
public class FallbackController {
    @GetMapping("/fallback/{app-name}")
    public Mono<CircuitBreakerFallbackMessage> getFallback(@PathVariable("app-name") String appName) {
        return Mono.just(new CircuitBreakerFallbackMessage(appName));
    }

    @NoArgsConstructor
    @Getter
    public static class CircuitBreakerFallbackMessage {
        private String message;

        public CircuitBreakerFallbackMessage(String message) {
            this.message = "%s is currently unavailable".formatted(message);
        }
    }
}
