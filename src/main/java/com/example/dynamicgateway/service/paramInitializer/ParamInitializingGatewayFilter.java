package com.example.dynamicgateway.service.paramInitializer;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;
import java.util.function.Function;

/**
 * A {@link GatewayFilter} that adds parameters to requests received by this Gateway
 * according to the injected {@link ParamInitializingStrategy strategy}
 */
public class ParamInitializingGatewayFilter implements GatewayFilter {
    private final String paramName;
    private final Function<ServerWebExchange, Flux<?>> paramValuesFunction;
    private final ParamInitializingStrategy strategy;

    /**
     * @param paramName           the name of a request parameter
     * @param paramValuesFunction a way to retrieve a {@code Flux} of
     *                            parameter values from the exchange
     * @param strategy            a strategy to add the parameters to the request's query map
     */
    public ParamInitializingGatewayFilter(String paramName,
                                          Function<ServerWebExchange, Flux<?>> paramValuesFunction,
                                          ParamInitializingStrategy strategy) {
        this.paramName = paramName;
        this.paramValuesFunction = paramValuesFunction;
        this.strategy = strategy;
    }

    /**
     * Applies the injected {@code paramValuesFunction} on the exchange, adds the parameters
     * published by the resulting {@code Flux} to the request, then filters the exchange further
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return paramValuesFunction.apply(exchange)
                .map(Object::toString)
                .collectList()
                .flatMap(paramValues -> changeExchangeAndFilter(exchange, chain, paramValues));
    }

    private Mono<Void> changeExchangeAndFilter(ServerWebExchange exchange, GatewayFilterChain chain,
                                               List<String> paramValues) {
        ServerWebExchange newExchange = buildNewExchange(exchange, paramValues);
        return chain.filter(newExchange);
    }

    private ServerWebExchange buildNewExchange(ServerWebExchange exchange, List<String> paramValues) {
        ServerHttpRequest newRequest = buildNewRequest(exchange.getRequest(), paramValues);
        return exchange
                .mutate()
                .request(newRequest)
                .build();
    }

    private ServerHttpRequest buildNewRequest(ServerHttpRequest request, List<String> paramValues) {
        URI newUri = buildNewUri(request, paramValues);
        return request
                .mutate()
                .uri(newUri)
                .build();
    }

    private URI buildNewUri(ServerHttpRequest request, List<String> paramValues) {
        MultiValueMap<String, String> oldQueryMap = request.getQueryParams();
        MultiValueMap<String, String> newQueryMap =
                strategy.apply(oldQueryMap, paramName, paramValues);
        return UriComponentsBuilder
                .fromUri(request.getURI())
                .replaceQueryParams(newQueryMap)
                .build()
                .toUri();
    }
}
