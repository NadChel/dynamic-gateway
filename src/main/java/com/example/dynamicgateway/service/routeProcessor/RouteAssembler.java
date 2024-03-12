package com.example.dynamicgateway.service.routeProcessor;

import org.springframework.cloud.gateway.route.Route;

/**
 * Interface to mark classes whose instances can mutate a {@link Route} builder based on the provided model
 *
 * @param <T> type of model this {@code RouteAssembler} can use to alter {@code Route} builders
 */
@FunctionalInterface
public interface RouteAssembler<T> {
    Route.AsyncBuilder process(Route.AsyncBuilder routeInConstruction, T model);
}
