package com.example.dynamicgateway.service.authenticationExtractor;

import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class CompositeAuthenticationExtractorTest {
    @Test
    void testTryExtractAuthentication_withoutDelegates_throws() {
        CompositeAuthenticationExtractor extractor = new CompositeAuthenticationExtractor(Collections.emptyList());
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/").build());
        StepVerifier.create(extractor.tryExtractAuthentication(exchange))
                .expectError(AuthenticationException.class)
                .verify();
    }

    @Test
    void testTryExtractAuthentication_withoutMatchingDelegates_throws() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/").build());
        AuthenticationExtractor delegateMock = mock(AuthenticationExtractor.class);
        given(delegateMock.isSupportedSource(exchange)).willReturn(false);

        CompositeAuthenticationExtractor extractor =
                new CompositeAuthenticationExtractor(List.of(delegateMock));

        StepVerifier.create(extractor.tryExtractAuthentication(exchange))
                .expectError(AuthenticationException.class)
                .verify();
    }

    @Test
    void testTryExtractAuthentication_withMatchingAuthenticators_delegates() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/").build());
        AuthenticationExtractor authenticatorMock = mock(AuthenticationExtractor.class);
        given(authenticatorMock.isSupportedSource(exchange)).willReturn(true);

        List<SimpleGrantedAuthority> roles = Stream.of("user")
                .map(SimpleGrantedAuthority::new)
                .toList();
        TestingAuthenticationToken authentication = new TestingAuthenticationToken("mickey_m", "password", roles);
        given(authenticatorMock.tryExtractAuthentication(exchange)).willReturn(Mono.just(authentication));

        CompositeAuthenticationExtractor compositeAuthenticationExtractor = new CompositeAuthenticationExtractor(List.of(authenticatorMock));

        StepVerifier.create(compositeAuthenticationExtractor.tryExtractAuthentication(exchange))
                .expectNext(authentication)
                .verifyComplete();
    }
}
