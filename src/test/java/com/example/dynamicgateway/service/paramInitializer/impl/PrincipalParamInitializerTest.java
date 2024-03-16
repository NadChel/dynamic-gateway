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
import static org.mockito.BDDMockito.given;

class PrincipalParamInitializerTest {
    private final PrincipalParamInitializer initializer = new PrincipalParamInitializer();
    private final String principalName = "mickey";

    @Test
    void extractValuesFromAuthentication_returnsExpectedPrincipal() {
        Authentication authenticationMock = mock(Authentication.class);
        given(authenticationMock.getName()).willReturn(principalName);

        Collection<?> paramValues = initializer.extractValuesFromAuthentication(authenticationMock);

        assertThat(paramValues.size()).isEqualTo(1);
        assertThat(paramValues.iterator().next()).isEqualTo(principalName);
    }

    @Test
    void getParamValues_returnsFluxOfExpectedPrincipal() {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(principalName, null);
        ServerWebExchange exchangeMock = mock(ServerWebExchange.class);
        given(exchangeMock.getPrincipal()).willReturn(Mono.just(authentication));

        assumeThatCode(() -> {
            Collection<?> paramValues = initializer.extractValuesFromAuthentication(authentication);
            assertThat(paramValues.size()).isEqualTo(1);
            assertThat(paramValues.iterator().next()).isEqualTo(principalName);
        }).doesNotThrowAnyException();

        StepVerifier.create(initializer.getParamValues(exchangeMock).map(String::valueOf))
                .expectNext(principalName)
                .verifyComplete();
    }
}