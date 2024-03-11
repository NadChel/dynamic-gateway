package com.example.dynamicgateway.service.authenticationExtractor;

import com.example.dynamicgateway.model.authorizationHeader.AuthorizationHeader;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

class AuthorizationHeaderAuthenticationExtractorTest {
    @Test
    void tryExtractAuthentication_ifNoAuthorizationHeader_returnsMonoOfAuthenticationException() {
        AuthenticationExtractor extractor = new AuthorizationHeaderAuthenticationExtractor() {
            @Override
            public Mono<Authentication> doTryExtractAuthentication(AuthorizationHeader authorizationHeader) {
                return Mono.empty();
            }

            @Override
            public boolean isSupportedAuthorizationHeader(AuthorizationHeader header) {
                return true;
            }
        };
        HttpHeaders emptyHeaders = HttpHeaders.EMPTY;
        MockServerHttpRequest request = MockServerHttpRequest.get("/")
                .headers(emptyHeaders)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        StepVerifier.create(extractor.tryExtractAuthentication(exchange))
                .expectError(AuthenticationException.class)
                .verify();
    }

    @Test
    void isSupportedSource_ifSupportedAuthorizationHeader_returnsTrue() {
        String supportedSchemeSpace = "supported-scheme ";
        AuthenticationExtractor extractor = new AuthorizationHeaderAuthenticationExtractor() {
            @Override
            public Mono<Authentication> doTryExtractAuthentication(AuthorizationHeader authorizationHeader) {
                return null;
            }

            @Override
            public boolean isSupportedAuthorizationHeader(AuthorizationHeader header) {
                return header.getScheme().equals(supportedSchemeSpace.trim());
            }
        };
        MockServerHttpRequest request = MockServerHttpRequest.get("/")
                .header(HttpHeaders.AUTHORIZATION, supportedSchemeSpace + "12345")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        assertThat(extractor.isSupportedSource(exchange)).isTrue();
    }

    @Test
    void isSupportedSource_ifNonSupportedAuthorizationHeader_returnsFalse() {
        String supportedSchemeSpace = "supported-scheme ";
        String nonSupportedSchemeSpace = "non-supported-scheme ";
        AuthenticationExtractor extractor = new AuthorizationHeaderAuthenticationExtractor() {
            @Override
            public Mono<Authentication> doTryExtractAuthentication(AuthorizationHeader authorizationHeader) {
                return null;
            }

            @Override
            public boolean isSupportedAuthorizationHeader(AuthorizationHeader header) {
                return header.getScheme().equals(supportedSchemeSpace.trim());
            }
        };
        MockServerHttpRequest request = MockServerHttpRequest.get("/")
                .header(HttpHeaders.AUTHORIZATION, nonSupportedSchemeSpace + "12345")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        assertThat(extractor.isSupportedSource(exchange)).isFalse();
    }
}