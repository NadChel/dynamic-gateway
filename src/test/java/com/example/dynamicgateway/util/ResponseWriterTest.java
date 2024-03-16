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
    void writeUnauthorizedResponse_sets401status_writesDetailMessageToBody_andReturnsEmptyMono() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/"));
        Throwable throwable = new AuthenticationException("Bad authentication request!");
        StepVerifier.create(ResponseWriter.writeUnauthorizedResponse(exchange, throwable))
                .verifyComplete();
        MockServerHttpResponse response = exchange.getResponse();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        StepVerifier.create(response.getBodyAsString())
                .expectNext(throwable.getMessage())
                .verifyComplete();
    }
}