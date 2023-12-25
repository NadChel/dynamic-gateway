package com.example.dynamicgateway.service.routeProcessor;

import com.example.dynamicgateway.model.documentedEndpoint.DocumentedEndpoint;
import org.springframework.cloud.gateway.route.Route;

/**
 * {@link RouteProcessor} that sources mutation data from supplied {@link DocumentedEndpoint}s
 */
@FunctionalInterface
public interface EndpointRouteProcessor extends RouteProcessor<DocumentedEndpoint<?>> {
    Route.AsyncBuilder process(Route.AsyncBuilder routeInConstruction, DocumentedEndpoint<?> endpoint);
}
