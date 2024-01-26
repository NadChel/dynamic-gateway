package com.example.dynamicgateway.service.paramInitializer.impl;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.Assumptions.assumeThatCode;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RolesParamInitializerTest {
    private final RolesParamInitializer initializer = new RolesParamInitializer();
    @Test
    void testExtractValuesFromAuthentication() {
        Authentication authenticationMock = mock(Authentication.class);
        List<GrantedAuthority> roles = List.of(
                new SimpleGrantedAuthority("user"),
                new SimpleGrantedAuthority("admin")
        );
        doReturn(roles).when(authenticationMock).getAuthorities();

        Collection<?> paramValues = initializer.extractValuesFromAuthentication(authenticationMock);

        assertThat(paramValues.size()).isEqualTo(2);
        assertThat(paramValues).asList().containsExactlyInAnyOrderElementsOf(roles);
    }

    @Test
    void testGetParamValues() {
        String userString = "user", admingString = "admin";
        List<SimpleGrantedAuthority> roles = Stream.of(userString, admingString).map(SimpleGrantedAuthority::new).toList();
        UsernamePasswordAuthenticationToken testAuthentication = new UsernamePasswordAuthenticationToken(
                null, null, roles
        );
        ServerWebExchange exchangeMock = mock(ServerWebExchange.class);
        when(exchangeMock.getPrincipal()).thenReturn(Mono.just(testAuthentication));

        assumeThatCode(() -> {
            Collection<?> paramValues = initializer.extractValuesFromAuthentication(testAuthentication);
            assertThat(paramValues.size()).isEqualTo(2);
            assertThat(paramValues).asList().containsExactlyInAnyOrderElementsOf(roles);
        }).doesNotThrowAnyException();

        StepVerifier.create(initializer.getParamValues(exchangeMock).map(String::valueOf))
                .expectNext(userString, admingString)
                .expectComplete()
                .verify();
    }
}