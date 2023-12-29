package com.example.dynamicgateway.service.paramResolver.impl;

import com.example.dynamicgateway.service.paramResolver.ParamInitializer;
import org.springframework.cloud.gateway.filter.OrderedGatewayFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Component
public class PrincipalParamInitializer implements ParamInitializer {
    @Override
    public String getInitializedParam() {
        return "principal";
    }

    @Override
    public void initialize(Route.AsyncBuilder routeInConstruction) {
        routeInConstruction.filter(new OrderedGatewayFilter(
                (exchange, chain) -> exchange.getPrincipal()
                        .flatMap(principal -> {
                            URI newUri = UriComponentsBuilder
                                    .fromUri(exchange.getRequest().getURI())
                                    .queryParam(getInitializedParam(), principal.getName())
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
                        }), 0));
    }
}
