package com.example.dynamicgateway.service.paramInitializer;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.OrderedGatewayFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;

import java.net.URI;

/**
 * Type that adds <em>parameter initializing filters</em> to provided {@link Route.AsyncBuilder}s.
 * Parameter initializing filters are {@link GatewayFilter}s that add parameters to requests
 * received by this Gateway
 */
public interface ParamInitializer {
    String getParamName();

    default void addInitializingFilter(Route.AsyncBuilder routeInConstruction) {
        routeInConstruction.filter(new OrderedGatewayFilter(
                initializingFilter(), 0
        ));
    }

    default GatewayFilter initializingFilter() {
        return (exchange, chain) -> getParamValues(exchange)
                .collectList()
                .flatMap(paramValues -> {
                    URI newUri = UriComponentsBuilder
                            .fromUri(exchange.getRequest().getURI())
                            .replaceQueryParam(getParamName(), paramValues)
                            .build()
                            .toUri();

                    ServerHttpRequest newRequest = exchange
                            .getRequest()
                            .mutate()
                            .uri(newUri)
                            .build();

                    ServerWebExchange newExchange = exchange
                            .mutate()
                            .request(newRequest)
                            .build();

                    return chain.filter(newExchange);
                });
    }

    Flux<?> getParamValues(ServerWebExchange exchange);
}
