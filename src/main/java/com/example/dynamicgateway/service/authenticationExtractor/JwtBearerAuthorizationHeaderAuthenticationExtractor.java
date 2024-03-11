package com.example.dynamicgateway.service.authenticationExtractor;

import com.example.dynamicgateway.constant.JWT;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;

/**
 * A {@link BearerAuthorizationHeaderAuthenticationExtractor} that parses JSON Web Tokens
 */
@Component
public class JwtBearerAuthorizationHeaderAuthenticationExtractor
        implements BearerAuthorizationHeaderAuthenticationExtractor {

    @Override
    public Mono<Authentication> doTryExtractAuthentication(String token) {
        return Mono.just(token)
                .map(this::getClaims)
                .onErrorMap(JwtException.class, this::toBadCredentialsException)
                .map(this::toUpat);
    }

    private Claims getClaims(String jwt) {
        SecretKey hashedKey = Keys.hmacShaKeyFor(JWT.KEY.getBytes(StandardCharsets.UTF_8));
        return Jwts.parserBuilder()
                .setSigningKey(hashedKey)
                .build()
                .parseClaimsJws(jwt)
                .getBody();
    }

    private BadCredentialsException toBadCredentialsException(JwtException e) {
        return new BadCredentialsException(
                MessageFormat.format("Invalid token. {0}", e.getMessage()), e);
    }

    private UsernamePasswordAuthenticationToken toUpat(Claims claims) {
        Object subject = getPrincipalName(claims);
        List<SimpleGrantedAuthority> authorities = getGrantedAuthorities(claims);

        return new UsernamePasswordAuthenticationToken(subject, null, authorities);
    }

    private Object getPrincipalName(Claims claims) {
        return claims.get(JWT.SUB);
    }

    private List<SimpleGrantedAuthority> getGrantedAuthorities(Claims claims) {
        Object nullableRoles = claims.get(JWT.ROLES);
        return (nullableRoles instanceof List<?> roles) ?
                buildGrantedAuthoritiesFrom(roles) :
                Collections.emptyList();
    }

    private List<SimpleGrantedAuthority> buildGrantedAuthoritiesFrom(List<?> roles) {
        return roles.stream()
                .map(Object::toString)
                .map(SimpleGrantedAuthority::new)
                .toList();
    }
}
