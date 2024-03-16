package com.example.dynamicgateway.service.authenticationExtractor;

import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class CompositeAuthenticationExtractorTest {
    @Test
    void isSupportedSource_returnsFalse_ifNoDelegatesInjected() {
        CompositeAuthenticationExtractor compositeExtractor =
                new CompositeAuthenticationExtractor(Collections.emptyList());
        ServerWebExchange exchangeMock = mock(ServerWebExchange.class);
        assertThat(compositeExtractor.isSupportedSource(exchangeMock)).isFalse();
    }

    @Test
    void isSupportedSource_returnsFalse_ifNoDelegateMatches() {
        ServerWebExchange exchangeMock = mock(ServerWebExchange.class);
        AuthenticationExtractor extractorMock = mock(AuthenticationExtractor.class);
        given(extractorMock.isSupportedSource(exchangeMock)).willReturn(false);
        CompositeAuthenticationExtractor compositeExtractor =
                new CompositeAuthenticationExtractor(List.of(extractorMock));
        assertThat(compositeExtractor.isSupportedSource(exchangeMock)).isFalse();
    }

    @Test
    void isSupportedSource_returnsTrue_ifDelegateMatches() {
        ServerWebExchange exchangeMock = mock(ServerWebExchange.class);
        AuthenticationExtractor extractorMock = mock(AuthenticationExtractor.class);
        given(extractorMock.isSupportedSource(exchangeMock)).willReturn(true);
        CompositeAuthenticationExtractor compositeExtractor =
                new CompositeAuthenticationExtractor(List.of(extractorMock));
        assertThat(compositeExtractor.isSupportedSource(exchangeMock)).isTrue();
    }

    @Test
    void testTryExtractAuthentication_withoutDelegates_throws() {
        CompositeAuthenticationExtractor compositeExtractor = new CompositeAuthenticationExtractor(Collections.emptyList());
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/"));
        StepVerifier.create(compositeExtractor.tryExtractAuthentication(exchange))
                .expectError(AuthenticationException.class)
                .verify();
    }

    @Test
    void testTryExtractAuthentication_withoutMatchingDelegates_throws() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/"));
        AuthenticationExtractor extractorMock = mock(AuthenticationExtractor.class);
        given(extractorMock.isSupportedSource(exchange)).willReturn(false);

        CompositeAuthenticationExtractor compositeExtractor =
                new CompositeAuthenticationExtractor(List.of(extractorMock));

        StepVerifier.create(compositeExtractor.tryExtractAuthentication(exchange))
                .expectError(AuthenticationException.class)
                .verify();
    }

    @Test
    void testTryExtractAuthentication_withMatchingDelegate_delegates() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/"));
        AuthenticationExtractor extractorMock = mock(AuthenticationExtractor.class);
        given(extractorMock.isSupportedSource(exchange)).willReturn(true);

        List<GrantedAuthority> roles = AuthorityUtils.createAuthorityList("user");
        TestingAuthenticationToken authentication =
                new TestingAuthenticationToken("mickey_m", "password", roles);
        given(extractorMock.doTryExtractAuthentication(exchange)).willReturn(Mono.just(authentication));

        AuthenticationExtractor compositeExtractor =
                new CompositeAuthenticationExtractor(List.of(extractorMock));

        StepVerifier.create(compositeExtractor.tryExtractAuthentication(exchange))
                .expectNext(authentication)
                .verifyComplete();
    }
}
