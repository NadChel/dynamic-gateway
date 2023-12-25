package by.afinny.apigateway.controller;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@Slf4j
public class FallbackController {
    @GetMapping("/fallback")
    public Mono<CircuitBreakerFallbackMessage> getFallback() {
        return Mono.just(new CircuitBreakerFallbackMessage());
    }

    @NoArgsConstructor
    @Getter
    public static class CircuitBreakerFallbackMessage {
        private final String message = "Service unavailable";
    }
}
