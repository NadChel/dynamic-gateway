package com.example.dynamicgateway.service.routeLocator;

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

import java.text.MessageFormat;
import java.util.HashSet;
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
        log.info(MessageFormat.format(
                "getRoutes() is invoked. The method is about to return a Flux of {0} route(s)",
                routes.size()
        ));
        return Flux.fromIterable(routes);
    }

    @EventListener
    public void onDocumentedEndpointFoundEvent(DocumentedEndpointFoundEvent event) {
        DocumentedEndpoint<?> foundEndpoint = event.getFoundEndpoint();
        Route route = transformToRoute(foundEndpoint);
        boolean isNewRouteAdded = routes.add(route);

        if (isNewRouteAdded) {
            log.info(MessageFormat.format("""
                            New route is built:
                            -----------------------------------
                            {0}
                            -----------------------------------
                            Will be available at the next getRoutes() invocation""",
                    route
            ));
        }
    }

    private Route transformToRoute(DocumentedEndpoint<?> documentedEndpoint) {
        Route.AsyncBuilder routeBuilder = Route.async();
        for (EndpointRouteProcessor endpointRouteProcessor : endpointRouteProcessors) {
            routeBuilder = endpointRouteProcessor.process(routeBuilder, documentedEndpoint);
        }
        return routeBuilder.build();
    }
}
