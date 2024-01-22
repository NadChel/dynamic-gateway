package com.example.dynamicgateway.service.paramInitializer.impl;

import com.example.dynamicgateway.service.paramInitializer.ParamInitializer;
import lombok.Getter;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Component
@Getter
public class PrincipalParamInitializer implements ParamInitializer {
    private final String paramName = "principal";

    @Override
    public GatewayFilter initializingFilter() {
        return (exchange, chain) -> exchange.getPrincipal()
                .flatMap(principal -> {
                    URI newUri = UriComponentsBuilder
                            .fromUri(exchange.getRequest().getURI())
                            .replaceQueryParam(getParamName(), principal.getName())
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
}
