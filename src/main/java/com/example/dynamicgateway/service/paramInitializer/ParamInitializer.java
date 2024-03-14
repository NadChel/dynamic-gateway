package com.example.dynamicgateway.service.paramInitializer;

import com.example.dynamicgateway.util.GatewayFilterUtil;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.OrderedGatewayFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;

/**
 * A supplier of {@link ParamInitializingGatewayFilter}s that could be added to {@link Route} builders
 */
public interface ParamInitializer {

    /**
     * Wraps the parameter initializing filter returned by {@link ParamInitializer#initializingFilter()}
     * in a new {@link OrderedGatewayFilter} with the order of zero and then adds it to
     * the passed-in {@code Route} builder
     *
     * @param routeInConstruction a {@code Route} builder to which the filter should be added
     * @see GatewayFilterUtil#wrapInOrderedGatewayFilter(GatewayFilter, int)
     */
    default void addInitializingFilter(Route.AbstractBuilder<?> routeInConstruction) {
        GatewayFilter initializingFilterWrapper =
                GatewayFilterUtil.wrapInOrderedGatewayFilter(initializingFilter());
        routeInConstruction.filter(initializingFilterWrapper);
    }

    /**
     * Returns a {@link ParamInitializingGatewayFilter} that adds to the request
     * {@link Object#toString() string representations} of parameter values published
     * by the {@code Flux} returned by {@link ParamInitializer#getParamValues(ServerWebExchange)}.
     * The values are associated with the key returned by {@link ParamInitializer#getParamName()}
     */
    default ParamInitializingGatewayFilter initializingFilter() {
        return new ParamInitializingGatewayFilter(
                getParamName(),
                this::getParamValues,
                getParamStrategy());
    }

    String getParamName();

    Flux<?> getParamValues(ServerWebExchange exchange);

    ParamInitializingStrategy getParamStrategy();
}
