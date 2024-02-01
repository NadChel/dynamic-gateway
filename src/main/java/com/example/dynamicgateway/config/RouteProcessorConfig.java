package com.example.dynamicgateway.config;

import com.example.dynamicgateway.model.discoverableApplication.DiscoverableApplication;
import com.example.dynamicgateway.model.documentedEndpoint.DocumentedEndpoint;
import com.example.dynamicgateway.model.endpointParameter.EndpointParameter;
import com.example.dynamicgateway.model.gatewayMeta.GatewayMeta;
import com.example.dynamicgateway.service.paramInitializer.ParamInitializer;
import com.example.dynamicgateway.service.paramInitializer.ParamInitializers;
import com.example.dynamicgateway.service.routeProcessor.EndpointRouteProcessor;
import lombok.RequiredArgsConstructor;
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
import org.springframework.core.annotation.Order;
import org.springframework.web.server.ServerWebExchange;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Configuration
@RequiredArgsConstructor
public class RouteProcessorConfig {
    private final GatewayMeta gatewayMeta;

    @Bean
    @Order(0)
    public EndpointRouteProcessor basePredicateProcessor() {
        return (routeInConstruction, endpoint) -> {
            setBasePredicate(routeInConstruction, endpoint);
            return routeInConstruction;
        };
    }

    /**
     * Sets base route predicate matching Gateway's prefix plus endpoint path stripped of non-semantic prefixes. For example,
     * if the passed endpoint's path is {@code /auth/example}, {@code /auth} is the endpoint's non-semantic prefix, and
     * Gateway's prefix is {@code /api/v1}, this method will set a base predicate matching {@code /api/v1/example}
     */
    private void setBasePredicate(Route.AsyncBuilder routeInConstruction, DocumentedEndpoint<?> endpoint) {
        String basePredicatePath = gatewayMeta.versionPrefix() + endpoint.getDetails().getNonPrefixedPath();
        addPredicate(routeInConstruction,
                new PathRoutePredicateFactory().applyAsync(
                        c -> c.setPatterns(List.of(basePredicatePath))
                ));
    }

    private void addPredicate(Route.AsyncBuilder routeInConstruction, AsyncPredicate<ServerWebExchange> predicate) {
        if (routeInConstruction.getPredicate() == null)
            routeInConstruction.asyncPredicate(predicate);
        else routeInConstruction.and(predicate);
    }

    @Bean
    @Order(1)
    public EndpointRouteProcessor removeGatewayPrefixRouteProcessor() {
        return (routeInConstruction, endpoint) -> {
            routeInConstruction.filter(
                    new OrderedGatewayFilter(
                            new RewritePathGatewayFilterFactory().apply(config -> config
                                    .setRegexp(gatewayMeta.versionPrefix())
                                    .setReplacement("")), 0)
            );
            return routeInConstruction;
        };
    }

    @Bean
    @Order(2)
    public EndpointRouteProcessor uriRouteProcessor() {
        return (routeInConstruction, endpoint) -> {
            DiscoverableApplication discoverableApp = endpoint.getDeclaringApp().getDiscoverableApp();
            return routeInConstruction.uri(discoverableApp.getDiscoveryServiceScheme() + discoverableApp.getName());
        };
    }

    @Bean
    @Order(3)
    public EndpointRouteProcessor appendEndpointPrefixRouteProcessor() {
        return (routeInConstruction, endpoint) -> {
            String endpointPrefix = endpoint.getDetails().getPrefix();
            if (!endpointPrefix.isEmpty()) {
                routeInConstruction.filter(
                        new OrderedGatewayFilter(
                                new PrefixPathGatewayFilterFactory().apply(config -> config
                                        .setPrefix(endpointPrefix)), 0)
                );
            }
            return routeInConstruction;
        };
    }

    @Bean
    @Order(4)
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
            routeInConstruction.filter(filterFactory.apply(routeInConstruction.getId(), config -> config.setFallbackUri("/fallback")));
            return routeInConstruction;
        };
    }
}
