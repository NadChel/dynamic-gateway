package com.example.dynamicgateway.service.authenticationExtractor;

import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class AuthenticationExtractorTest {
    @Test
    void tryExtractAuthentication_wrapsOtherExceptionsInAuthenticationException() {
        Throwable originalThrowable = new UnknownError();
        AuthenticationExtractor authenticationExtractor = new AuthenticationExtractor() {
            @Override
            public Mono<Authentication> doTryExtractAuthentication(ServerWebExchange exchange) {
                return Mono.error(originalThrowable);
            }

            @Override
            public boolean isSupportedSource(ServerWebExchange exchange) {
                return true;
            }
        };
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/"));
        StepVerifier.create(authenticationExtractor.tryExtractAuthentication(exchange))
                .expectErrorMatches(t -> t instanceof AuthenticationException &&
                        t.getCause().equals(originalThrowable))
                .verify();
    }

    @Test
    void tryExtractAuthentication_ifExceptionAlreadyAuthenticationException_doesntDoAnyWrapping() {
        Throwable originalThrowable = new BadCredentialsException("Invalid password");
        AuthenticationExtractor authenticationExtractor = new AuthenticationExtractor() {
            @Override
            public Mono<Authentication> doTryExtractAuthentication(ServerWebExchange exchange) {
                return Mono.error(originalThrowable);
            }

            @Override
            public boolean isSupportedSource(ServerWebExchange exchange) {
                return true;
            }
        };
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/"));
        StepVerifier.create(authenticationExtractor.tryExtractAuthentication(exchange))
                .expectErrorMatches(t -> t == originalThrowable)
                .verify();
    }
}