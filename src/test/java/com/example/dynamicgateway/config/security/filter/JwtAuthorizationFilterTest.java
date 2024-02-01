package com.example.dynamicgateway.config.security.filter;

import com.example.dynamicgateway.constant.JWT;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpResponse;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.context.Context;

import javax.crypto.SecretKey;
import java.sql.Date;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.Assumptions.assumeThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JwtAuthorizationFilterTest {
    private final JwtAuthorizationFilter filter = new JwtAuthorizationFilter();

    @Test
    void testFilter_withNullJwt_exchangeFilteredFurther() {
        ServerWebExchange mockExchange = MockServerWebExchange.from(MockServerHttpRequest.get("/"));

        WebFilterChain mockChain = mock(WebFilterChain.class);
        when(mockChain.filter(mockExchange)).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(mockExchange, mockChain))
                .expectComplete()
                .verify();

        verify(mockChain).filter(mockExchange);
    }

    @Test
    void testFilter_withValidJwt_exchangeFilteredFurther() {
        SecretKey key = Keys.hmacShaKeyFor(JWT.KEY.getBytes());

        String validJwt = Jwts.builder()
                .setExpiration(Date.valueOf(LocalDate.now().plusYears(1)))
                .signWith(key)
                .compact();

        MockServerHttpRequest request = MockServerHttpRequest
                .get("/")
                .header(JWT.HEADER, validJwt)
                .build();

        ServerWebExchange mockExchange = MockServerWebExchange.from(request);

        WebFilterChain mockChain = mock(WebFilterChain.class);
        when(mockChain.filter(mockExchange)).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(mockExchange, mockChain))
                .expectComplete()
                .verify();

        verify(mockChain).filter(mockExchange);
    }

    @Test
    void testFilter_withValidJwt_authenticationPassedToContext() {
        SecretKey key = Keys.hmacShaKeyFor(JWT.KEY.getBytes());

        String sub = "mickey_m";
        List<String> roles = List.of("user", "admin");

        String validJwt = Jwts.builder()
                .claim(JWT.SUB, sub)
                .claim(JWT.ROLES, roles)
                .setExpiration(Date.valueOf(LocalDate.now().plusYears(1)))
                .signWith(key)
                .compact();

        MockServerHttpRequest request = MockServerHttpRequest
                .get("/")
                .header(JWT.HEADER, validJwt)
                .build();

        ServerWebExchange mockExchange = MockServerWebExchange.from(request);

        WebFilterChain mockChain = mock(WebFilterChain.class);
        when(mockChain.filter(mockExchange)).thenReturn(Mono.empty());

        assumeThatCode(() -> {
            StepVerifier.create(filter.filter(mockExchange, mockChain))
                    .expectComplete()
                    .verify();
            verify(mockChain).filter(mockExchange);
        }).doesNotThrowAnyException();

        StepVerifier.create(filter.filter(mockExchange, mockChain))
                .expectAccessibleContext()
                .assertThat(context -> contextHasExpectedAuthentication(context, sub, roles))
                .then()
                .expectComplete()
                .verify();
    }

    private void contextHasExpectedAuthentication(Context context, String subject, Collection<String> roles) {
        boolean doesAnyContextEntryHaveExpectedAuthentication = context.stream()
                .anyMatch(entry -> hasExpectedAuthentication(entry, subject, roles));
        assertThat(doesAnyContextEntryHaveExpectedAuthentication).isTrue();
    }

    @SuppressWarnings("unchecked")
    private boolean hasExpectedAuthentication(Map.Entry<Object, Object> contextEntry, String subject, Collection<String> roles) {
        return !doesThrow(() ->
                StepVerifier.create((Mono<SecurityContext>) contextEntry.getValue())
                        .expectNextMatches(securityContext -> {
                            Authentication auth = securityContext.getAuthentication();
                            return auth.getPrincipal().equals(subject) &&
                                    auth.getAuthorities().stream()
                                            .map(GrantedAuthority::getAuthority)
                                            .toList()
                                            .containsAll(roles);
                        })
                        .expectComplete()
                        .verify());
    }

    private boolean doesThrow(Runnable codeBlock) {
        try {
            codeBlock.run();
            return false;
        } catch (Exception e) {
            return true;
        }
    }

    @Test
    void testFilter_withExpiredJwt() {
        SecretKey key = Keys.hmacShaKeyFor(JWT.KEY.getBytes());

        String expiredJwt = Jwts.builder()
                .setExpiration(Date.valueOf(LocalDate.now().minusDays(1)))
                .signWith(key)
                .compact();

        testForInvalidJwt(expiredJwt);
    }

    private void testForInvalidJwt(String jwt) {
        MockServerHttpRequest mockRequest = MockServerHttpRequest
                .get("/")
                .header(JWT.HEADER, jwt)
                .build();

        ServerWebExchange mockExchange = MockServerWebExchange.from(mockRequest);

        WebFilterChain mockChain = mock(WebFilterChain.class);

        StepVerifier.create(filter.filter(mockExchange, mockChain))
                .expectComplete()
                .verify();

        verify(mockChain, never()).filter(mockExchange);

        ServerHttpResponse response = mockExchange.getResponse();

        assertThat(response).extracting(ServerHttpResponse::getStatusCode).isEqualTo(HttpStatus.UNAUTHORIZED);

        StepVerifier.create(((MockServerHttpResponse) response).getBodyAsString())
                .expectNextMatches(StringUtils::isNotBlank)
                .expectComplete()
                .verify();
    }

    @Test
    void testFilter_withPhonySigningKey() {
        SecretKey phonyKey = Keys.hmacShaKeyFor(UUID.randomUUID().toString().getBytes());

        String jwtWithPhonyKey = Jwts.builder()
                .setExpiration(Date.valueOf(LocalDate.now().plusYears(1)))
                .signWith(phonyKey)
                .compact();

        testForInvalidJwt(jwtWithPhonyKey);
    }

    @Test
    void testFilter_withMalformedJwt() {
        SecretKey key = Keys.hmacShaKeyFor(JWT.KEY.getBytes());

        String malformedJwt = Jwts.builder()
                .setExpiration(Date.valueOf(LocalDate.now().plusYears(1)))
                .signWith(key)
                .compact() + ".somebs";

        testForInvalidJwt(malformedJwt);
    }
}
