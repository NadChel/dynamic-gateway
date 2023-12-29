package com.example.dynamicgateway.service.paramResolver;

import org.springframework.cloud.gateway.route.Route;

public interface ParamInitializer {
    String getInitializedParam();

    void initialize(Route.AsyncBuilder routeInConstruction);
}
