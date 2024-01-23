package com.example.dynamicgateway.service.paramInitializer.impl;

import com.example.dynamicgateway.service.paramInitializer.ParamInitializer;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Collection;

public abstract class SecurityParamInitializer implements ParamInitializer {
    @Override
    public GatewayFilter initializingFilter() {
        return (exchange, chain) -> exchange.getPrincipal()
                .flatMap(principal -> {
                    Collection<?> paramValues = extractValuesFromAuthentication((Authentication) principal);

                    URI newUri = UriComponentsBuilder
                            .fromUri(exchange.getRequest().getURI())
                            .replaceQueryParam(getParamName(), paramValues)
                            .build(true)
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

    public abstract Collection<?> extractValuesFromAuthentication(Authentication authentication);
}
