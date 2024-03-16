package com.example.dynamicgateway.service.paramInitializer.impl;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThatCode;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willReturn;
import static org.mockito.Mockito.mock;

class RolesParamInitializerTest {
    private final RolesParamInitializer initializer = new RolesParamInitializer();

    @Test
    void extractValuesFromAuthentication_returnsExpectedRoles() {
        List<GrantedAuthority> roles = AuthorityUtils.createAuthorityList("user", "admin");
        Authentication authenticationMock = mock(Authentication.class);
        willReturn(roles).given(authenticationMock).getAuthorities();

        Collection<?> paramValues = initializer.extractValuesFromAuthentication(authenticationMock);

        assertThat(paramValues.size()).isEqualTo(2);
        assertThat(paramValues).asList().containsExactlyInAnyOrderElementsOf(roles);
    }

    @Test
    void getParamValues_returnsFluxOfExpectedRoles() {
        List<GrantedAuthority> roles = AuthorityUtils.createAuthorityList("user", "admin");
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                null, null, roles
        );
        ServerWebExchange exchangeMock = mock(ServerWebExchange.class);
        given(exchangeMock.getPrincipal()).willReturn(Mono.just(authentication));

        assumeThatCode(() -> {
            Collection<?> paramValues = initializer.extractValuesFromAuthentication(authentication);
            assertThat(paramValues.size()).isEqualTo(2);
            assertThat(paramValues).asList().containsExactlyInAnyOrderElementsOf(roles);
        }).doesNotThrowAnyException();

        StepVerifier.create(initializer.getParamValues(exchangeMock).map(GrantedAuthority.class::cast))
                .recordWith(ArrayList::new)
                .expectNextCount(2)
                .expectRecordedMatches(recordedRoles ->
                        recordedRoles.containsAll(roles) &&
                                roles.containsAll(recordedRoles))
                .expectComplete()
                .verify();
    }
}