package com.example.dynamicgateway.config;

import com.example.dynamicgateway.model.discoverableApplication.DiscoverableApplication;
import com.example.dynamicgateway.model.documentedEndpoint.DocumentedEndpoint;
import com.example.dynamicgateway.model.endpointParameter.EndpointParameter;
import com.example.dynamicgateway.model.gatewayMeta.GatewayMeta;
import com.example.dynamicgateway.service.paramInitializer.ParamInitializer;
import com.example.dynamicgateway.service.paramInitializer.ParamInitializers;
import com.example.dynamicgateway.service.routeProcessor.EndpointRouteAssembler;
import com.example.dynamicgateway.util.EndpointUtil;
import com.example.dynamicgateway.util.GatewayFilterUtil;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.OrderedGatewayFilter;
import org.springframework.cloud.gateway.filter.factory.PrefixPathGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.RewritePathGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.SpringCloudCircuitBreakerFilterFactory;
import org.springframework.cloud.gateway.handler.AsyncPredicate;
import org.springframework.cloud.gateway.handler.predicate.MethodRoutePredicateFactory;
import org.springframework.cloud.gateway.handler.predicate.PathRoutePredicateFactory;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.server.ServerWebExchange;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Configuration
public class RouteAssemblerConfig {
    private final GatewayMeta gatewayMeta;

    public RouteAssemblerConfig(GatewayMeta gatewayMeta) {
        this.gatewayMeta = gatewayMeta;
    }

    @Bean
    public EndpointRouteAssembler pathPredicateRouteAssembler() {
        return (routeInConstruction, endpoint) -> {
            addPathPredicate(routeInConstruction, endpoint);
            return routeInConstruction;
        };
    }

    /**
     * Adds a path {@link AsyncPredicate} to the provided {@code Route} builder.
     * The predicate will match requests whose path equals the result of concatenation of
     * this Gateway's {@link GatewayMeta#getVersionPrefix() version prefix} with the endpoint
     * path stripped of an {@link GatewayMeta#getIgnoredPrefixes() ignored prefix}
     * (if the endpoint path starts with one). For example, if the passed endpoint's path is
     * {@code /auth/example}, {@code /auth} is an ignored prefix, and this Gateway's prefix
     * is {@code /api/v1}, this method will add a predicate matching {@code /api/v1/example}
     */
    private void addPathPredicate(Route.AsyncBuilder routeInConstruction, DocumentedEndpoint<?> endpoint) {
        String nonPrefixedPath = EndpointUtil.pathWithRemovedPrefix(endpoint, gatewayMeta);
        String matchingPath = gatewayMeta.getVersionPrefix() + nonPrefixedPath;
        AsyncPredicate<ServerWebExchange> pathPredicate = new PathRoutePredicateFactory()
                .applyAsync(config -> config.setPatterns(List.of(matchingPath)));
        addPredicate(routeInConstruction, pathPredicate);
    }

    private static void addPredicate(Route.AsyncBuilder routeInConstruction, AsyncPredicate<ServerWebExchange> predicate) {
        if (routeInConstruction.getPredicate() == null)
            routeInConstruction.asyncPredicate(predicate);
        else routeInConstruction.and(predicate);
    }

    @Bean
    public EndpointRouteAssembler versionPrefixRemovingRouteAssembler() {
        return (routeInConstruction, endpoint) -> {
            addVersionPrefixRemovingFilter(routeInConstruction);
            return routeInConstruction;
        };
    }

    private void addVersionPrefixRemovingFilter(Route.AsyncBuilder routeInConstruction) {
        GatewayFilter versionPrefixRemovingFilter = new RewritePathGatewayFilterFactory().apply(config -> config
                .setRegexp(gatewayMeta.getVersionPrefix())
                .setReplacement(""));
        wrapAndAddFilter(routeInConstruction, versionPrefixRemovingFilter);
    }

    /**
     * Wraps the filter in an {@link OrderedGatewayFilter} with the order of zero and adds the
     * resulting wrapper to the {@code Route} builder
     */
    private static void wrapAndAddFilter(Route.AsyncBuilder routeInConstruction, GatewayFilter filterToWrapAndAdd) {
        GatewayFilter wrappedFilter = GatewayFilterUtil.wrapInOrderedGatewayFilter(filterToWrapAndAdd);
        routeInConstruction.filter(wrappedFilter);
    }

    @Bean
    public EndpointRouteAssembler uriRouteAssembler() {
        return (routeInConstruction, endpoint) -> {
            DiscoverableApplication<?> discoverableApp = endpoint.getDeclaringApp().getDiscoverableApp();
            String uri = discoverableApp.getDiscoveryServiceScheme() + discoverableApp.getName();
            return routeInConstruction.uri(uri);
        };
    }

    @Bean
    public EndpointRouteAssembler ignoredPrefixAppendingRouteAssembler() {
        return (routeInConstruction, endpoint) -> {
            String endpointPrefix = EndpointUtil.pathPrefix(endpoint, gatewayMeta);
            if (!endpointPrefix.isEmpty())
                addIgnoredPrefixFilter(routeInConstruction, endpointPrefix);
            return routeInConstruction;
        };
    }

    private static void addIgnoredPrefixFilter(Route.AsyncBuilder routeInConstruction, String endpointPrefix) {
        GatewayFilter prefixPathFilter = new PrefixPathGatewayFilterFactory()
                .apply(config -> config.setPrefix(endpointPrefix));
        wrapAndAddFilter(routeInConstruction, prefixPathFilter);
    }

    @Bean
    public EndpointRouteAssembler idRouteAssembler() {
        return (routeInConstruction, endpoint) -> routeInConstruction.id(UUID.randomUUID().toString());
    }

    @Bean
    public EndpointRouteAssembler methodRouteAssembler() {
        return (routeInConstruction, endpoint) -> {
            addMethodPredicate(routeInConstruction, endpoint);
            return routeInConstruction;
        };
    }

    private static void addMethodPredicate(Route.AsyncBuilder routeInConstruction, DocumentedEndpoint<?> endpoint) {
        HttpMethod endpointMethod = endpoint.getDetails().getMethod();
        AsyncPredicate<ServerWebExchange> methodPredicate = new MethodRoutePredicateFactory()
                .applyAsync(c -> c.setMethods(endpointMethod));
        addPredicate(routeInConstruction, methodPredicate);
    }

    @Bean
    public EndpointRouteAssembler paramInitializingRouteAssembler(ParamInitializers paramInitializers) {
        return (routeInConstruction, endpoint) -> {
            for (EndpointParameter param : endpoint.getDetails().getParameters()) {
                Optional<ParamInitializer> optionalParamInitializer = paramInitializers.findInitializerForParam(param);
                optionalParamInitializer.ifPresent(
                        paramInitializer -> paramInitializer.addInitializingFilter(routeInConstruction)
                );
            }
            return routeInConstruction;
        };
    }

    @Bean
    public EndpointRouteAssembler circuitBreakerEndpointRouteAssembler(SpringCloudCircuitBreakerFilterFactory filterFactory) {
        return (routeInConstruction, endpoint) -> {
            String declaringAppName = endpoint.getDeclaringApp().getName();
            GatewayFilter circuitBreakerFilter = filterFactory.apply(
                    routeInConstruction.getId(),
                    config -> config
                            .setFallbackUri("/fallback/" + declaringAppName)
                            .setName("Default circuit breaker")
            );
            wrapAndAddFilter(routeInConstruction, circuitBreakerFilter);
            return routeInConstruction;
        };
    }
}
