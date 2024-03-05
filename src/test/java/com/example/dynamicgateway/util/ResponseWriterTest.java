package com.example.dynamicgateway.util;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpResponse;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.test.StepVerifier;

import javax.naming.AuthenticationException;

import static org.assertj.core.api.Assertions.assertThat;

class ResponseWriterTest {
    @Test
    void testWriteUnauthorizedResponse() {
        MockServerWebExchange exchange = MockServerWebExchange.builder(MockServerHttpRequest.get("/")).build();
        Throwable exception = new AuthenticationException("Bad authentication request!");
        StepVerifier.create(ResponseWriter.writeUnauthorizedResponse(exchange, exception))
                .verifyComplete();
        MockServerHttpResponse response = exchange.getResponse();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        StepVerifier.create(response.getBodyAsString())
                .expectNext(exception.getMessage())
                .verifyComplete();
    }
}