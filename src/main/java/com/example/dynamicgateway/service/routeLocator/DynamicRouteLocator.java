package com.example.dynamicgateway.service.routeLocator;

import com.example.dynamicgateway.events.DiscoverableApplicationLostEvent;
import com.example.dynamicgateway.events.DocumentedEndpointFoundEvent;
import com.example.dynamicgateway.model.documentedEndpoint.DocumentedEndpoint;
import com.example.dynamicgateway.service.routeProcessor.EndpointRouteProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * {@link RouteLocator} that dynamically supplies {@link Route}s built from discovered {@link DocumentedEndpoint}s
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class DynamicRouteLocator implements RouteLocator {
    private final Set<Route> routes = new HashSet<>();
    private final List<EndpointRouteProcessor> endpointRouteProcessors;

    @Override
    public Flux<Route> getRoutes() {
        log.info("getRoutes() is invoked. The method is about to return a Flux of {} route(s)",
                routes.size());
        return Flux.fromIterable(routes);
    }

    @EventListener
    public void onDocumentedEndpointFoundEvent(DocumentedEndpointFoundEvent event) {
        DocumentedEndpoint<?> foundEndpoint = event.getFoundEndpoint();
        Route route = transformToRoute(foundEndpoint);
        boolean isNewRouteAdded = routes.add(route);

        if (isNewRouteAdded) {
            log.info("""
                            New route is built:
                            -----------------------------------
                            {}
                            -----------------------------------
                            Will be available at the next getRoutes() invocation""",
                    route);
        }
    }

    private Route transformToRoute(DocumentedEndpoint<?> documentedEndpoint) {
        Route.AsyncBuilder routeBuilder = Route.async();
        for (EndpointRouteProcessor endpointRouteProcessor : endpointRouteProcessors) {
            routeBuilder = endpointRouteProcessor.process(routeBuilder, documentedEndpoint);
        }
        return routeBuilder.build();
    }

    @EventListener
    public void onDiscoverableApplicationLostEvent(DiscoverableApplicationLostEvent event) {
        for (Iterator<Route> iterator = routes.iterator(); iterator.hasNext(); ) {
            Route route = iterator.next();
            String routesAppName = route.getUri().getHost();
            String lostAppName = event.getLostApp().getName();
            if (routesAppName.equals(lostAppName)) {
                iterator.remove();
                log.info("Route {} serviced by {} was evicted",
                        route.getId(), lostAppName);
            }
        }
    }
}
