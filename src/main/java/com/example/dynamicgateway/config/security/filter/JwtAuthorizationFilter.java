package com.example.dynamicgateway.config.security.filter;

import com.example.dynamicgateway.constant.JWT;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

/**
 * WebFilter that extracts a JWT token from the request header and verifies its signature using a secret key.
 * If the token is valid, it sets the authentication object in the SecurityContextHolder.
 */
@Slf4j
public class JwtAuthorizationFilter implements WebFilter {

    /**
     * Filters the incoming request and sets the authentication object in the SecurityContextHolder if the JWT token is valid.
     *
     * @param exchange the ServerWebExchange object representing the incoming request and outgoing response
     * @param chain    the WebFilterChain object representing the filter chain to be executed
     * @return a Mono<Void> representing the completion of the filter chain
     */
    @Override
    @SuppressWarnings("NullableProblems")
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String jwt = exchange.getRequest().getHeaders().getFirst(JWT.HEADER);

        if (jwt != null) {
            try {
                return authenticateAndFilter(exchange, chain, jwt);
            } catch (ExpiredJwtException e) {
                log.error("Token has expired. " + e.getMessage());
                return createError(exchange, e);
            } catch (SignatureException e) {
                log.error("Token signature cannot be verified. " + e.getMessage());
                return createError(exchange, e);
            } catch (MalformedJwtException e) {
                log.error("Token is not properly formatted. " + e.getMessage());
                return createError(exchange, e);
            } catch (Exception e) {
                log.error("Error parsing token. " + e.getMessage());
                return createError(exchange, e);
            }
        }
        return chain.filter(exchange);
    }

    private Mono<Void> authenticateAndFilter(ServerWebExchange exchange, WebFilterChain chain, String jwt) {
        UsernamePasswordAuthenticationToken upat = buildUpat(jwt);
        return chain.filter(exchange).contextWrite(ReactiveSecurityContextHolder.withAuthentication(upat));
    }

    private UsernamePasswordAuthenticationToken buildUpat(String jwt) {
        Claims claims = getClaims(jwt);

        Object clientId = getPrincipalName(claims);
        List<? extends GrantedAuthority> authorities = getGrantedAuthorities(claims);

        return new UsernamePasswordAuthenticationToken(clientId, null, authorities);
    }

    private Claims getClaims(String jwt) {
        SecretKey hashedKey = Keys.hmacShaKeyFor(JWT.KEY.getBytes(StandardCharsets.UTF_8));

        return Jwts.parserBuilder()
                .setSigningKey(hashedKey)
                .build()
                .parseClaimsJws(jwt)
                .getBody();
    }

    private Object getPrincipalName(Claims claims) {
        return claims.get(JWT.SUB);
    }

    private List<? extends GrantedAuthority> getGrantedAuthorities(Claims claims) {
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

    private Mono<Void> createError(ServerWebExchange exchange, Exception e) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        return response.writeWith(Mono.just(createErrorBody(e.getMessage())));
    }

    private DataBuffer createErrorBody(String errorBody) {
        byte[] bytes = errorBody.getBytes();
        return new DefaultDataBufferFactory().wrap(bytes);
    }
}