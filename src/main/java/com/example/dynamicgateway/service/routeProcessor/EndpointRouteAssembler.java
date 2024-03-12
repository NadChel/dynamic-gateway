package com.example.dynamicgateway.service.routeProcessor;

import com.example.dynamicgateway.model.documentedEndpoint.DocumentedEndpoint;
import org.springframework.cloud.gateway.route.Route;

/**
 * A {@link RouteAssembler} that models {@code Route} builders after supplied {@link DocumentedEndpoint}s
 */
@FunctionalInterface
public interface EndpointRouteAssembler extends RouteAssembler<DocumentedEndpoint<?>> {
    Route.AsyncBuilder process(Route.AsyncBuilder routeInConstruction, DocumentedEndpoint<?> endpoint);
}
