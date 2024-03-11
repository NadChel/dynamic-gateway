package com.example.dynamicgateway.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.text.MessageFormat;

@RestController
@Slf4j
public class FallbackController {
    @GetMapping("/fallback/{app-name}")
    public Mono<ResponseEntity<String>> getFallback(@PathVariable("app-name") String appName) {
        return Mono.just(ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT)
                .body(buildMessage(appName)));
    }

    private static String buildMessage(String appName) {
        return MessageFormat.format("{0} is currently unavailable", appName);
    }
}
