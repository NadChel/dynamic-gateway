package com.example.dynamicgateway.service.routeProcessor;

import com.example.dynamicgateway.model.documentedApplication.DocumentedApplication;
import org.springframework.cloud.gateway.route.Route;

/**
 * {@link RouteProcessor} that sources mutation data from supplied {@link DocumentedApplication}s
 */
@FunctionalInterface
public interface ApplicationRouteProcessor extends RouteProcessor<DocumentedApplication<?>> {
    Route.AsyncBuilder process(Route.AsyncBuilder routeInConstruction, DocumentedApplication<?> endpoint);
}
