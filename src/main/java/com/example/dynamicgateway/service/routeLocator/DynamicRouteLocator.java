package com.example.dynamicgateway.service.routeLocator;

import com.example.dynamicgateway.events.DiscoverableApplicationLostEvent;
import com.example.dynamicgateway.events.DocumentedEndpointFoundEvent;
import com.example.dynamicgateway.model.discoverableApplication.DiscoverableApplication;
import com.example.dynamicgateway.model.documentedEndpoint.DocumentedEndpoint;
import com.example.dynamicgateway.service.routeProcessor.EndpointRouteAssembler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A {@link RouteLocator} that dynamically supplies {@link Route}s built from
 * {@link DocumentedEndpointFoundEvent found} {@link DocumentedEndpoint}s
 */
@Component
@Slf4j
public class DynamicRouteLocator implements RouteLocator {
    private final Set<Route> routes = ConcurrentHashMap.newKeySet();
    private final List<EndpointRouteAssembler> endpointRouteAssemblers;

    public DynamicRouteLocator(List<EndpointRouteAssembler> endpointRouteAssemblers) {
        this.endpointRouteAssemblers = endpointRouteAssemblers;
    }

    /**
     * Returns a {@code Flux} of all endpoints contained in this {@code RouteLocator}'s
     * {@code Route} collection
     */
    @Override
    public Flux<Route> getRoutes() {
        log.info("getRoutes() is invoked. The method is about to return a Flux of {} route(s)",
                routes.size());
        return Flux.fromIterable(routes);
    }

    /**
     * Assembles a new {@code Route} with the injected {@link EndpointRouteAssembler}s and then
     * adds it to this {@code RouteLocator}s {@code Route} collection
     *
     * @param event the carrier of the found {@code DocumentedEndpoint}
     */
    @EventListener
    public void onDocumentedEndpointFoundEvent(DocumentedEndpointFoundEvent event) {
        Mono.justOrEmpty(event)
                .mapNotNull(DocumentedEndpointFoundEvent::getFoundEndpoint)
                .map(this::transformToRoute)
                .filter(routes::add)
                .subscribe(route -> log.info("""
                            New route is built:
                            -----------------------------------
                            {}
                            -----------------------------------
                            Will be available at the next getRoutes() invocation
                        """, route));
    }

    private Route transformToRoute(DocumentedEndpoint<?> documentedEndpoint) {
        Route.AsyncBuilder routeBuilder = Route.async();
        for (EndpointRouteAssembler assembler : endpointRouteAssemblers) {
            routeBuilder = assembler.process(routeBuilder, documentedEndpoint);
        }
        return routeBuilder.build();
    }

    /**
     * Evicts all {@code Route}s built after either of the lost application's endpoints.
     * This method makes such a connection by comparing the hosts of
     * its {@code Route}s' {@link Route#getUri() URIs} with the lost application's
     * {@link DiscoverableApplication#getName() name}. A {@code Route} is evicted if {@code equals()}
     * between the two returns {@code true}
     *
     * @param event the carrier of the lost application
     */
    @EventListener
    public void onDiscoverableApplicationLostEvent(DiscoverableApplicationLostEvent event) {
        for (Iterator<Route> iterator = routes.iterator(); iterator.hasNext(); ) {
            Route route = iterator.next();
            String routesAppName = route.getUri().getHost();
            String lostAppName = event.getLostApp().getName();
            if (routesAppName.equals(lostAppName)) {
                iterator.remove();
                log.info("Route {} serviced by lost {} was evicted",
                        route.getId(), lostAppName);
            }
        }
    }
}
