package com.example.dynamicgateway.service.paramInitializer.impl;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Collection;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.Assumptions.assumeThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PrincipalParamInitializerTest {
    private final PrincipalParamInitializer initializer = new PrincipalParamInitializer();
    private final String principalName = "mickey";

    @Test
    void testExtractValuesFromAuthentication() {
        Authentication authenticationMock = mock(Authentication.class);
        when(authenticationMock.getName()).thenReturn(principalName);

        Collection<?> paramValues = initializer.extractValuesFromAuthentication(authenticationMock);

        assertThat(paramValues.size()).isEqualTo(1);
        assertThat(paramValues.iterator().next()).isEqualTo(principalName);
    }

    @Test
    void testGetParamValues() {
        UsernamePasswordAuthenticationToken authenticationFake = new UsernamePasswordAuthenticationToken(principalName, null);
        ServerWebExchange exchangeMock = mock(ServerWebExchange.class);
        when(exchangeMock.getPrincipal()).thenReturn(Mono.just(authenticationFake));

        assumeThatCode(() -> {
            Collection<?> paramValues = initializer.extractValuesFromAuthentication(authenticationFake);
            assertThat(paramValues.size()).isEqualTo(1);
            assertThat(paramValues.iterator().next()).isEqualTo(principalName);
        }).doesNotThrowAnyException();

        StepVerifier.create(initializer.getParamValues(exchangeMock).map(String::valueOf))
                .expectNext(principalName)
                .expectComplete()
                .verify();
    }
}