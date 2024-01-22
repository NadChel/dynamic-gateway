package com.example.dynamicgateway.service.paramInitializer;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.OrderedGatewayFilter;
import org.springframework.cloud.gateway.route.Route;

public interface ParamInitializer {
    String getParamName();

    default void initialize(Route.AsyncBuilder routeInConstruction) {
        routeInConstruction.filter(new OrderedGatewayFilter(
                initializingFilter(), 0
        ));
    }

    GatewayFilter initializingFilter();
}
