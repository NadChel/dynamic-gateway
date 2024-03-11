package com.example.dynamicgateway.service.authenticationExtractor;

import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * An {@link AuthenticationExtractor} that extracts an {@code Authentication} object from the request's headers
 */
public interface HeaderAuthenticationExtractor extends AuthenticationExtractor {
    @Override
    default Mono<Authentication> doTryExtractAuthentication(ServerWebExchange exchange) {
        return Mono.just(exchange)
                .map(ServerWebExchange::getRequest)
                .map(ServerHttpRequest::getHeaders)
                .flatMap(this::doTryExtractAuthentication);
    }

    Mono<Authentication> doTryExtractAuthentication(HttpHeaders headers);

    @Override
    default boolean isSupportedSource(ServerWebExchange exchange) {
        HttpHeaders headers = exchange.getRequest().getHeaders();
        return areSupportedHeaders(headers);
    }

    boolean areSupportedHeaders(HttpHeaders headers);
}
