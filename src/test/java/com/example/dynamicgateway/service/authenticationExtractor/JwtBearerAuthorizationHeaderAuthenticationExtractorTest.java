package com.example.dynamicgateway.service.authenticationExtractor;

import com.example.dynamicgateway.constant.JWT;
import com.example.dynamicgateway.model.authorizationHeader.AuthorizationHeader;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.AuthorityUtils;
import reactor.test.StepVerifier;

import javax.crypto.SecretKey;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtBearerAuthorizationHeaderAuthenticationExtractorTest {
    private final JwtBearerAuthorizationHeaderAuthenticationExtractor extractor = new JwtBearerAuthorizationHeaderAuthenticationExtractor();

    @Test
    void tryExtractAuthentication_withValidToken_returnsExpectedAuthentication() {
        String principal = "mickey_m";
        List<String> roleStrings = List.of("user", "admin");

        SecretKey key = Keys.hmacShaKeyFor(JWT.KEY.getBytes());

        String validToken = Jwts.builder()
                .claim(JWT.SUB, principal)
                .claim(JWT.ROLES, roleStrings)
                .setExpiration(Date.valueOf(LocalDate.now().plusYears(1)))
                .signWith(key)
                .compact();
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/")
                .header(HttpHeaders.AUTHORIZATION, AuthorizationHeader.BEARER_SPACE + validToken)
                .build());

        UsernamePasswordAuthenticationToken expectedAuthentication =
                UsernamePasswordAuthenticationToken.authenticated(principal, null,
                        AuthorityUtils.createAuthorityList(roleStrings));

        StepVerifier.create(extractor.tryExtractAuthentication(exchange))
                .expectNext(expectedAuthentication)
                .verifyComplete();
    }

    @Test
    void tryExtractAuthentication_withExpiredToken_throwsAuthenticationException() {
        SecretKey key = Keys.hmacShaKeyFor(JWT.KEY.getBytes());

        String expiredToken = Jwts.builder()
                .setExpiration(Date.valueOf(LocalDate.now().minusDays(1)))
                .signWith(key)
                .compact();

        assertTryExtractAuthenticationThrowsAuthenticationExceptionWithToken(expiredToken);
    }

    private void assertTryExtractAuthenticationThrowsAuthenticationExceptionWithToken(String invalidToken) {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/")
                .header(HttpHeaders.AUTHORIZATION, AuthorizationHeader.BEARER_SPACE + invalidToken)
                .build());

        StepVerifier.create(extractor.tryExtractAuthentication(exchange))
                .expectError(AuthenticationException.class)
                .verify();
    }

    @Test
    void tryExtractAuthentication_withPhonySigningKey_throwsAuthenticationException() {
        SecretKey phonyKey = Keys.hmacShaKeyFor(UUID.randomUUID().toString().getBytes());

        String jwtWithPhonyKey = Jwts.builder()
                .setExpiration(Date.valueOf(LocalDate.now().plusYears(1)))
                .signWith(phonyKey)
                .compact();

        assertTryExtractAuthenticationThrowsAuthenticationExceptionWithToken(jwtWithPhonyKey);
    }

    @Test
    void tryExtractAuthentication_withMalformedToken_throwsAuthenticationException() {
        SecretKey key = Keys.hmacShaKeyFor(JWT.KEY.getBytes());

        String malformedToken = Jwts.builder()
                .setExpiration(Date.valueOf(LocalDate.now().plusYears(1)))
                .signWith(key)
                .compact() + ".somebs";

        assertTryExtractAuthenticationThrowsAuthenticationExceptionWithToken(malformedToken);
    }

    @Test
    void tryExtractAuthentication_withTokenWithNoRoles_setsEmptyCollection() {
        SecretKey key = Keys.hmacShaKeyFor(JWT.KEY.getBytes());

        String tokenWithNoRoles = Jwts.builder()
                .setExpiration(Date.valueOf(LocalDate.now().plusYears(1)))
                .signWith(key)
                .compact();

        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/")
                .header(HttpHeaders.AUTHORIZATION, AuthorizationHeader.BEARER_SPACE + tokenWithNoRoles)
                .build());

        StepVerifier.create(extractor.tryExtractAuthentication(exchange))
                .assertNext(authentication -> {
                    assertThat(authentication.getAuthorities()).isNotNull();
                    assertThat(authentication.getAuthorities()).isEmpty();
                })
                .verifyComplete();
    }
}