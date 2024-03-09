package com.example.dynamicgateway.config;

import com.example.dynamicgateway.model.discoverableApplication.DiscoverableApplication;
import com.example.dynamicgateway.model.documentedEndpoint.DocumentedEndpoint;
import com.example.dynamicgateway.model.endpointParameter.EndpointParameter;
import com.example.dynamicgateway.model.gatewayMeta.GatewayMeta;
import com.example.dynamicgateway.service.paramInitializer.ParamInitializer;
import com.example.dynamicgateway.service.paramInitializer.ParamInitializers;
import com.example.dynamicgateway.service.routeProcessor.EndpointRouteProcessor;
import com.example.dynamicgateway.util.EndpointUtil;
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
import org.springframework.web.server.ServerWebExchange;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Configuration
public class RouteProcessorConfig {
    private final GatewayMeta gatewayMeta;

    public RouteProcessorConfig(GatewayMeta gatewayMeta) {
        this.gatewayMeta = gatewayMeta;
    }

    @Bean
    public EndpointRouteProcessor basePredicateProcessor() {
        return (routeInConstruction, endpoint) -> {
            setBasePredicate(routeInConstruction, endpoint);
            return routeInConstruction;
        };
    }

    /**
     * Sets base route predicate matching Gateway's prefix plus endpoint path stripped of the ignored prefix. For example,
     * if the passed endpoint's path is {@code /auth/example}, {@code /auth} is an ignored prefix, and
     * Gateway's prefix is {@code /api/v1}, this method will set a base predicate matching {@code /api/v1/example}
     *
     * @see GatewayMeta#getVersionPrefix()
     * @see GatewayMeta#getIgnoredPrefixes()
     */
    private void setBasePredicate(Route.AsyncBuilder routeInConstruction, DocumentedEndpoint<?> endpoint) {
        String nonPrefixedPath = EndpointUtil.withRemovedPrefix(endpoint, gatewayMeta);
        String basePredicatePath = gatewayMeta.getVersionPrefix() + nonPrefixedPath;
        addPredicate(routeInConstruction,
                new PathRoutePredicateFactory().applyAsync(
                        c -> c.setPatterns(List.of(basePredicatePath))
                ));
    }

    private static void addPredicate(Route.AsyncBuilder routeInConstruction, AsyncPredicate<ServerWebExchange> predicate) {
        if (routeInConstruction.getPredicate() == null)
            routeInConstruction.asyncPredicate(predicate);
        else routeInConstruction.and(predicate);
    }

    @Bean
    public EndpointRouteProcessor removeGatewayPrefixRouteProcessor() {
        return (routeInConstruction, endpoint) -> {
            routeInConstruction.filter(wrapInOrderedGatewayFilter(
                    new RewritePathGatewayFilterFactory().apply(config -> config
                            .setRegexp(gatewayMeta.getVersionPrefix())
                            .setReplacement(""))
            ));
            return routeInConstruction;
        };
    }

    /**
     * Wraps the passed {@link GatewayFilter} in an instance of {@link OrderedGatewayFilter}.
     * At the point of this writing, some filters, such as the one produced by a
     * {@link PrefixPathGatewayFilterFactory}, don't work unless wrapped in such a way
     *
     * @param wrappee filter to wrap
     * @return {@code OrderedGatewayFilter} with order of zero
     */
    private static GatewayFilter wrapInOrderedGatewayFilter(GatewayFilter wrappee) {
        return new OrderedGatewayFilter(wrappee, 0);
    }

    @Bean
    public EndpointRouteProcessor uriRouteProcessor() {
        return (routeInConstruction, endpoint) -> {
            DiscoverableApplication<?> discoverableApp = endpoint.getDeclaringApp().getDiscoverableApp();
            return routeInConstruction.uri(discoverableApp.getDiscoveryServiceScheme() + discoverableApp.getName());
        };
    }

    @Bean
    public EndpointRouteProcessor appendEndpointPrefixRouteProcessor() {
        return (routeInConstruction, endpoint) -> {
            String endpointPrefix = EndpointUtil.extractPrefix(endpoint, gatewayMeta);
            if (!endpointPrefix.isEmpty()) {
                routeInConstruction.filter(wrapInOrderedGatewayFilter(
                        new PrefixPathGatewayFilterFactory().apply(config -> config
                                .setPrefix(endpointPrefix))
                ));
            }
            return routeInConstruction;
        };
    }

    @Bean
    public EndpointRouteProcessor idRouteProcessor() {
        return (routeInConstruction, endpoint) -> routeInConstruction.id(UUID.randomUUID().toString());
    }

    @Bean
    public EndpointRouteProcessor methodRouteProcessor() {
        return (routeInConstruction, endpoint) -> {
            appendHttpMethodPredicate(routeInConstruction, endpoint);
            return routeInConstruction;
        };
    }

    private void appendHttpMethodPredicate(Route.AsyncBuilder routeInConstruction, DocumentedEndpoint<?> endpoint) {
        addPredicate(routeInConstruction,
                new MethodRoutePredicateFactory().applyAsync(
                        c -> c.setMethods(endpoint.getDetails().getMethod()))
        );
    }

    @Bean
    public EndpointRouteProcessor paramInitializingRouteProcessor(ParamInitializers paramInitializers) {
        return (routeInConstruction, endpoint) -> {
            for (EndpointParameter param : endpoint.getDetails().getParameters()) {
                Optional<ParamInitializer> optionalParamInitializer = paramInitializers.findInitializerForParam(param);
                optionalParamInitializer.ifPresent(
                        paramInitializer -> paramInitializer.initialize(routeInConstruction)
                );
            }
            return routeInConstruction;
        };
    }

    @Bean
    public EndpointRouteProcessor circuitBreakerEndpointRouteProcessor(SpringCloudCircuitBreakerFilterFactory filterFactory) {
        return (routeInConstruction, endpoint) -> {
            routeInConstruction.filter(wrapInOrderedGatewayFilter(
                    filterFactory.apply(routeInConstruction.getId(),
                            config -> config.setFallbackUri("/fallback/" + endpoint.getDeclaringApp().getName())
                    )));
            return routeInConstruction;
        };
    }
}
