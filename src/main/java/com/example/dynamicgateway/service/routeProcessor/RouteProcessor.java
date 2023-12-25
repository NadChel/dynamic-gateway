package by.afinny.apigateway.service.routeProcessor;

import org.springframework.cloud.gateway.route.Route;

/**
 * Interface to mark classes whose instances can mutate a {@link Route} builder based on the provided model
 *
 * @param <T> type of model this {@code RouteProcessor} can use to alter {@code Route} builders
 */
@FunctionalInterface
public interface RouteProcessor<T> {
    Route.AsyncBuilder process(Route.AsyncBuilder routeInConstruction, T model);
}
