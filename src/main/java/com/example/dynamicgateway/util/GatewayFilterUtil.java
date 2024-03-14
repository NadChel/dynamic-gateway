package com.example.dynamicgateway.util;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.OrderedGatewayFilter;
import org.springframework.cloud.gateway.filter.factory.PrefixPathGatewayFilterFactory;

public class GatewayFilterUtil {
    /**
     * A shorthand for {@code GatewayFilterUtil.wrapInOrderedGatewayFilter(gatewayFilter, 0)}
     *
     * @see GatewayFilterUtil#wrapInOrderedGatewayFilter(GatewayFilter, int)
     */
    public static GatewayFilter wrapInOrderedGatewayFilter(GatewayFilter wrappee) {
        return wrapInOrderedGatewayFilter(wrappee, 0);
    }
    /**
     * Wraps the passed {@link GatewayFilter} in an instance of {@link OrderedGatewayFilter}.
     * At the point of this writing, some filters, such as the one produced by a
     * {@link PrefixPathGatewayFilterFactory}, may not work unless wrapped in such a way
     *
     * @param wrappee a filter to wrap
     * @return an {@code OrderedGatewayFilter} with the specified order
     */
    public static GatewayFilter wrapInOrderedGatewayFilter(GatewayFilter wrappee, int order) {
        return new OrderedGatewayFilter(wrappee, order);
    }
}
