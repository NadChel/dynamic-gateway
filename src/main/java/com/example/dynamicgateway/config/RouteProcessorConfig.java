package com.example.dynamicgateway.config;

import com.example.dynamicgateway.model.discoverableApplication.DiscoverableApplication;
import com.example.dynamicgateway.model.documentedEndpoint.DocumentedEndpoint;
import com.example.dynamicgateway.model.gatewayMeta.GatewayMeta;
import com.example.dynamicgateway.service.routeProcessor.EndpointRouteProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.OrderedGatewayFilter;
import org.springframework.cloud.gateway.filter.factory.RewritePathGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.SpringCloudCircuitBreakerFilterFactory;
import org.springframework.cloud.gateway.handler.predicate.MethodRoutePredicateFactory;
import org.springframework.cloud.gateway.handler.predicate.PathRoutePredicateFactory;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
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
        routeInConstruction.asyncPredicate(
                new PathRoutePredicateFactory()
                        .applyAsync(c -> c.setPatterns(List.of(gatewayMeta.v1Prefix() + endpoint.getDetails().getNonPrefixedPath())))
        );
    }

    @Bean
    @Order(1)
    public EndpointRouteProcessor idRouteProcessor() {
        return (routeInConstruction, endpoint) -> routeInConstruction.id(UUID.randomUUID().toString());
    }

    @Bean
    public EndpointRouteProcessor uriRouteProcessor() {
        return (routeInConstruction, endpoint) -> {
            DiscoverableApplication discoverableApp = endpoint.getDeclaringApp().getDiscoverableApp();
            return routeInConstruction.uri(discoverableApp.getDiscoveryServiceScheme() + discoverableApp.getName());
        };
    }

    @Bean
    public EndpointRouteProcessor methodRouteProcessor() {
        return (routeInConstruction, endpoint) -> {
            appendHttpMethodPredicate(routeInConstruction, endpoint);
            return routeInConstruction;
        };
    }

    private void appendHttpMethodPredicate(Route.AsyncBuilder routeInConstruction, DocumentedEndpoint<?> endpoint) {
        routeInConstruction.and(
                new MethodRoutePredicateFactory().applyAsync(c ->
                        c.setMethods(endpoint.getDetails().getMethod()))
        );
    }

    @Bean
    public EndpointRouteProcessor authenticationRouteProcessor() {
        return (routeInConstruction, endpoint) -> {
            if (hasAuthenticatedTag(endpoint)) {
                replaceGatewayPrefixWithAuth(routeInConstruction);
                addPrincipalNameAsRequestParam(routeInConstruction);
            }
            return routeInConstruction;
        };
    }

    private boolean hasAuthenticatedTag(DocumentedEndpoint<?> endpoint) {
        return endpoint.getDetails()
                .getTags()
                .stream()
                .anyMatch(tag -> tag.equals("AUTHENTICATED"));
    }

    private void replaceGatewayPrefixWithAuth(Route.AsyncBuilder routeInConstruction) {
        routeInConstruction.filter(
                new OrderedGatewayFilter(
                        new RewritePathGatewayFilterFactory().apply(config -> config
                                .setRegexp(gatewayMeta.v1Prefix())
                                .setReplacement("/auth")), 0)
        );
    }

    private void addPrincipalNameAsRequestParam(Route.AsyncBuilder routeInConstruction) {
        routeInConstruction.filter(new OrderedGatewayFilter(
                (exchange, chain) -> exchange.getPrincipal()
                        .flatMap(principal -> {
                            URI newUri = UriComponentsBuilder
                                    .fromUri(exchange.getRequest().getURI())
                                    .queryParam("clientId", principal.getName())
                                    .build(true)
                                    .toUri();

                            ServerHttpRequest newRequest = exchange
                                    .getRequest()
                                    .mutate()
                                    .uri(newUri)
                                    .build();

                            ServerWebExchange newExchange = exchange
                                    .mutate()
                                    .request(newRequest)
                                    .build();

                            return chain.filter(newExchange);
                        }), 0));
    }

    @Bean
    public EndpointRouteProcessor circuitBreakerEndpointRouteProcessor(SpringCloudCircuitBreakerFilterFactory filterFactory) {
        return (routeInConstruction, endpoint) -> {
            routeInConstruction.filter(filterFactory.apply(routeInConstruction.getId(), config -> config.setFallbackUri("/fallback")));
            return routeInConstruction;
        };
    }
}
