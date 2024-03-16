package com.example.dynamicgateway.controller;

import com.example.dynamicgateway.controller.config.EnableMockAuthentication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.HttpStatus;
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
    void getFallback_returnsExpectedFallback() {
        String appName = "some-app";
        String fallbackMessage = testClient
                .get()
                .uri(MessageFormat.format("/fallback/{0}", appName))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.GATEWAY_TIMEOUT)
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();
        assertThat(fallbackMessage).isNotNull();
        assertThat(fallbackMessage)
                .containsPattern(Pattern.compile(appName + " (is )?(currently )?unavailable"));
    }
}