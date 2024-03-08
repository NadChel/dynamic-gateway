package com.example.dynamicgateway.controller;

import com.example.dynamicgateway.controller.FallbackController.CircuitBreakerFallbackMessage;
import com.example.dynamicgateway.controller.config.EnableMockAuthentication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.text.MessageFormat;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

@WebFluxTest(controllers = FallbackController.class)
@EnableMockAuthentication
@ActiveProfiles("test")
class FallbackControllerTest {
    @Autowired
    WebTestClient testClient;

    @Test
    void testGetFallback() {
        String appName = "some-app";
        CircuitBreakerFallbackMessage fallbackMessage = testClient
                .get()
                .uri(MessageFormat.format("/fallback/{0}", appName))
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(CircuitBreakerFallbackMessage.class)
                .returnResult()
                .getResponseBody();
        assertThat(fallbackMessage)
                .extracting(CircuitBreakerFallbackMessage::getMessage)
                .asString()
                .containsPattern(Pattern.compile(appName + " (is )?(currently )?unavailable"));
    }
}