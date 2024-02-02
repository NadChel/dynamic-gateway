package com.example.dynamicgateway.testUtil;

import lombok.SneakyThrows;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.route.Route;

import java.lang.reflect.Field;
import java.net.URI;
import java.util.List;

public class RouteBuilderUtil {
    @SuppressWarnings("unchecked")
    public static List<GatewayFilter> getFilters(Route.AsyncBuilder routeBuilder) {
        return (List<GatewayFilter>) getAbstractBuilderField(routeBuilder, "gatewayFilters");
    }

    @SuppressWarnings("SameParameterValue")
    private static Object getAbstractBuilderField(Route.AsyncBuilder routeBuilder, String fieldName) {
        return getAbstractBuilderField(routeBuilder, fieldName, Object.class);
    }

    @SneakyThrows
    private static <T> T getAbstractBuilderField(Route.AsyncBuilder routeBuilder, String fieldName, Class<T> clazz) {
        Field gatewayFilters = routeBuilder.getClass()
                .getSuperclass()
                .getDeclaredField(fieldName);
        gatewayFilters.setAccessible(true);
        return clazz.cast(gatewayFilters.get(routeBuilder));
    }

    public static URI getUri(Route.AsyncBuilder routeBuilder) {
        return getAbstractBuilderField(routeBuilder, "uri", URI.class);
    }
}
