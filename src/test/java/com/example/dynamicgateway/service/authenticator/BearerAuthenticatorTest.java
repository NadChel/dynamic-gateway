package com.example.dynamicgateway.service.authenticator;

import com.example.dynamicgateway.constant.JWT;
import com.example.dynamicgateway.model.authorizationHeader.AuthorizationHeader;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;

import javax.crypto.SecretKey;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BearerAuthenticatorTest {
    static final String BEARER_SPACE = "bearer ";

    @Test
    void testBuildAuthentication_withValidToken() {
        String principal = "mickey_m";
        List<String> roles = List.of("user", "admin");

        SecretKey key = Keys.hmacShaKeyFor(JWT.KEY.getBytes());

        String validToken = Jwts.builder()
                .claim(JWT.SUB, principal)
                .claim(JWT.ROLES, roles)
                .setExpiration(Date.valueOf(LocalDate.now().plusYears(1)))
                .signWith(key)
                .compact();
        AuthorizationHeader header = new AuthorizationHeader(BEARER_SPACE + validToken);
        BearerAuthenticator authenticator = new BearerAuthenticator(header);

        assertThatCode(authenticator::buildAuthentication).doesNotThrowAnyException();

        Authentication authentication = authenticator.buildAuthentication();
        assertThat(authentication.getPrincipal()).isEqualTo(principal);
        assertThat(authentication.getAuthorities()).map(GrantedAuthority::getAuthority).isEqualTo(roles);
    }

    @Test
    void testBuildAuthentication_withExpiredToken() {
        SecretKey key = Keys.hmacShaKeyFor(JWT.KEY.getBytes());

        String expiredToken = Jwts.builder()
                .setExpiration(Date.valueOf(LocalDate.now().minusDays(1)))
                .signWith(key)
                .compact();

        testForInvalidToken(expiredToken);
    }

    private void testForInvalidToken(String invalidToken) {
        AuthorizationHeader header = new AuthorizationHeader(BEARER_SPACE + invalidToken);
        BearerAuthenticator authenticator = new BearerAuthenticator(header);

        assertThatThrownBy(authenticator::buildAuthentication).isInstanceOf(AuthenticationException.class);
    }

    @Test
    void testBuildAuthentication_withPhonySigningKey() {
        SecretKey phonyKey = Keys.hmacShaKeyFor(UUID.randomUUID().toString().getBytes());

        String jwtWithPhonyKey = Jwts.builder()
                .setExpiration(Date.valueOf(LocalDate.now().plusYears(1)))
                .signWith(phonyKey)
                .compact();

        testForInvalidToken(jwtWithPhonyKey);
    }

    @Test
    void testBuildAuthentication_withMalformedToken() {
        SecretKey key = Keys.hmacShaKeyFor(JWT.KEY.getBytes());

        String malformedToken = Jwts.builder()
                .setExpiration(Date.valueOf(LocalDate.now().plusYears(1)))
                .signWith(key)
                .compact() + ".somebs";

        testForInvalidToken(malformedToken);
    }
}