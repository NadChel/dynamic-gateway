package com.example.dynamicgateway.config.security.filter;

import com.example.dynamicgateway.model.authorizationHeader.AuthorizationHeader;
import com.example.dynamicgateway.service.authenticator.Authenticator;
import jakarta.ws.rs.core.HttpHeaders;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * A {@link WebFilter} that tries to build an {@link Authentication} object from the request's {@code Authorization} header
 * and pass it to the {@link SecurityContext}.
 * <p>
 * This class is oblivious to the header's authentication scheme and relies on an injected {@link Authenticator} object
 * to actually parse a credentials token contained in the header
 */
@Slf4j
@Component
public class AuthenticationWebFilter implements WebFilter {
    private final Authenticator authenticator;

    public AuthenticationWebFilter(Authenticator authenticator) {
        this.authenticator = authenticator;
    }

    /**
     * Filters the incoming request by examining the {@code Authorization} header and then trying to build
     * an {@link Authentication} instance from it and put it in the {@link SecurityContext}
     * <p>
     * If the request doesn't have the header or the header has no value, the filter simply forwards the request
     * down the filter chain
     * <p>
     * If authentication fails – for example, due to a missing or unsupported authentication scheme or invalid
     * credentials – the method sets the 401 Unauthorized status code to the response
     */
    @Override
    @SuppressWarnings("NullableProblems")
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String authorizationHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authorizationHeader != null) {
            try {
                return authenticateAndFilter(exchange, chain, authorizationHeader);
            } catch (AuthenticationException e) {
                return writeUnauthorizedResponse(exchange, e.getMessage());
            } catch (Exception e) {
                return writeInternalServerErrorResponse(exchange, e.getMessage());
            }
        }

        return chain.filter(exchange);
    }

    private Mono<Void> authenticateAndFilter(ServerWebExchange exchange, WebFilterChain chain, String rawHeader) {
        AuthorizationHeader authorizationHeader = new AuthorizationHeader(rawHeader);
        Authentication authentication = authenticator
                .tryExtractAuthentication(authorizationHeader);
        return chain.filter(exchange).contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
    }

    private Mono<Void> writeUnauthorizedResponse(ServerWebExchange exchange, String errorMessage) {
        return writeErrorResponse(exchange, HttpStatus.UNAUTHORIZED, errorMessage);
    }

    private Mono<Void> writeInternalServerErrorResponse(ServerWebExchange exchange, String errorMessage) {
        return writeErrorResponse(exchange, HttpStatus.INTERNAL_SERVER_ERROR, errorMessage);
    }

    private Mono<Void> writeErrorResponse(ServerWebExchange exchange, HttpStatus status, String errorMessage) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        return response.writeWith(Mono.just(createErrorBody(errorMessage)));
    }

    private DataBuffer createErrorBody(String errorBody) {
        byte[] bytes = errorBody.getBytes();
        return new DefaultDataBufferFactory().wrap(bytes);
    }
}