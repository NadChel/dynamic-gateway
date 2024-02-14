package com.example.dynamicgateway.config.security.filter;

import com.example.dynamicgateway.model.authorizationHeader.AuthorizationHeader;
import com.example.dynamicgateway.service.authenticator.Authenticator;
import jakarta.ws.rs.core.HttpHeaders;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpResponse;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.context.Context;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThatCode;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class AuthenticationWebFilterTest {
    private final String validToken = "imagine it's a valid token";
    @InjectMocks
    private AuthenticationWebFilter filter;
    @Mock
    private Authenticator authenticatorMock;
    @Mock
    private WebFilterChain chainMock;

    @Test
    void testFilter_withNullToken_exchangeFilteredFurther() {
        ServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/"));
        given(chainMock.filter(exchange)).willReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, chainMock))
                .expectComplete()
                .verify();

        then(chainMock).should().filter(exchange);
    }

    @Test
    void testFilter_withValidToken_exchangeFilteredFurther() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/")
                .header(HttpHeaders.AUTHORIZATION, validToken)
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        AuthorizationHeader authorizationHeader = new AuthorizationHeader(validToken);
        TestingAuthenticationToken authenticationToken =
                new TestingAuthenticationToken("some principal", "some credentials");
        given(authenticatorMock.tryExtractAuthentication(authorizationHeader)).willReturn(authenticationToken);

        given(chainMock.filter(exchange)).willReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, chainMock))
                .expectComplete()
                .verify();

        then(chainMock).should().filter(exchange);
    }

    @Test
    void testFilter_withValidToken_authenticationPassedToContext() {
        String sub = "mickey_m";
        List<String> roles = List.of("user", "admin");
        TestingAuthenticationToken authenticationToken = new TestingAuthenticationToken(
                sub, null, roles.stream().map(SimpleGrantedAuthority::new).toList()
        );

        AuthorizationHeader authorizationHeader = new AuthorizationHeader(validToken);
        given(authenticatorMock.tryExtractAuthentication(authorizationHeader)).willReturn(authenticationToken);

        MockServerHttpRequest request = MockServerHttpRequest
                .get("/")
                .header(HttpHeaders.AUTHORIZATION, validToken)
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        given(chainMock.filter(exchange)).willReturn(Mono.empty());

        assumeThatCode(() -> {
            StepVerifier.create(filter.filter(exchange, chainMock))
                    .expectComplete()
                    .verify();
            then(chainMock).should().filter(exchange);
        }).doesNotThrowAnyException();

        StepVerifier.create(filter.filter(exchange, chainMock))
                .expectAccessibleContext()
                .assertThat(context -> contextHasExpectedAuthentication(context, sub, roles))
                .then()
                .expectComplete()
                .verify();
    }

    private void contextHasExpectedAuthentication(Context context, String subject, Collection<String> roles) {
        boolean someContextEntryHasExpectedAuthentication = context.stream()
                .anyMatch(entry -> hasExpectedAuthentication(entry, subject, roles));

        assertThat(someContextEntryHasExpectedAuthentication)
                .withFailMessage("None of Context entries contained expected Authentication")
                .isTrue();
    }

    @SuppressWarnings("unchecked")
    private boolean hasExpectedAuthentication(Map.Entry<Object, Object> contextEntry, String subject, Collection<String> roles) {
        return !doesThrow(() ->
                StepVerifier.create((Mono<SecurityContext>) contextEntry.getValue())
                        .assertNext(securityContext -> {
                            Authentication auth = securityContext.getAuthentication();

                            assertThat(auth.getPrincipal()).isEqualTo(subject);
                            assertThat(auth.getAuthorities())
                                    .extracting(GrantedAuthority::getAuthority)
                                    .containsExactlyInAnyOrderElementsOf(roles);
                        })
                        .expectComplete()
                        .verify());
    }

    private boolean doesThrow(Runnable codeBlock) {
        try {
            codeBlock.run();
            return false;
        } catch (Throwable t) {
            return true;
        }
    }

    @Test
    void testFilter_withInvalidToken() {
        String invalidToken = "it's an invalid token";
        AuthorizationHeader authorizationHeader = new AuthorizationHeader(invalidToken);

        given(authenticatorMock.tryExtractAuthentication(authorizationHeader)).willThrow(new BadCredentialsException("Invalid token"));

        MockServerHttpRequest request = MockServerHttpRequest
                .get("/")
                .header(HttpHeaders.AUTHORIZATION, invalidToken)
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, chainMock))
                .expectComplete()
                .verify();

        then(chainMock).should(never()).filter(exchange);

        ServerHttpResponse response = exchange.getResponse();
        assertThat(response).extracting(ServerHttpResponse::getStatusCode).isEqualTo(HttpStatus.UNAUTHORIZED);

        StepVerifier.create(((MockServerHttpResponse) response).getBodyAsString())
                .expectNextMatches(StringUtils::isNotBlank)
                .expectComplete()
                .verify();
    }
}
