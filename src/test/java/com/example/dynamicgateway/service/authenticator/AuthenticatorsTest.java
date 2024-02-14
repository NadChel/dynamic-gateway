package com.example.dynamicgateway.service.authenticator;

import com.example.dynamicgateway.model.authorizationHeader.AuthorizationHeader;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class AuthenticatorsTest {
    @Test
    void testTryExtractAuthentication_withoutActualAuthenticators_throws() {
        Authenticators authenticators = new Authenticators(Collections.emptyList());
        AuthorizationHeader header = new AuthorizationHeader("bearer 1234567890");
        assertThatThrownBy(() -> authenticators.tryExtractAuthentication(header)).isInstanceOf(AuthenticationException.class);
    }

    @Test
    void testTryExtractAuthentication_withoutMatchingAuthenticators_throws() {
        AuthorizationHeader header = new AuthorizationHeader("bearer 1234567890");
        LeafAuthenticator authenticatorMock = mock(LeafAuthenticator.class);
        given(authenticatorMock.hasSupportedScheme(header)).willReturn(false);

        Authenticators authenticators = new Authenticators(List.of(authenticatorMock));

        assertThatThrownBy(() -> authenticators.tryExtractAuthentication(header)).isInstanceOf(AuthenticationException.class);
    }

    @Test
    void testTryExtractAuthentication_withMatchingAuthenticators_delegates() {
        AuthorizationHeader header = new AuthorizationHeader("bearer 1234567890");
        LeafAuthenticator authenticatorMock = mock(LeafAuthenticator.class);
        given(authenticatorMock.hasSupportedScheme(header)).willReturn(true);

        List<SimpleGrantedAuthority> roles = Stream.of("user")
                .map(SimpleGrantedAuthority::new)
                .toList();
        TestingAuthenticationToken authentication = new TestingAuthenticationToken("mickey_m", "password", roles);
        given(authenticatorMock.extractAuthentication(header)).willReturn(authentication);

        Authenticators authenticators = new Authenticators(List.of(authenticatorMock));

        assertThat(authenticators.tryExtractAuthentication(header)).isEqualTo(authentication);
    }
}
