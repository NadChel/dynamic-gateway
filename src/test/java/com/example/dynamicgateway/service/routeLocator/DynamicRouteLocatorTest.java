package com.example.dynamicgateway.service.routeLocator;

import com.example.dynamicgateway.events.DocumentedEndpointFoundEvent;
import com.example.dynamicgateway.service.routeProcessor.EndpointRouteProcessor;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.route.Route;
import reactor.test.StepVerifier;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import static org.mockito.Mockito.mock;

class DynamicRouteLocatorTest {
    private DynamicRouteLocator dynamicRouteLocator;

    @Test
    void ifNoEventsFired_returnsEmptyFlux() {
        dynamicRouteLocator = new DynamicRouteLocator(Collections.emptyList());

        StepVerifier.create(dynamicRouteLocator.getRoutes())
                .expectNextCount(0)
                .expectComplete()
                .verify();
    }

    @Test
    void ifOneEventFired_returnsFluxOfOneRoute() {
        dynamicRouteLocator = new DynamicRouteLocator(getEndpointRouteProcessorMock());

        DocumentedEndpointFoundEvent mockEvent = mock(DocumentedEndpointFoundEvent.class);

        dynamicRouteLocator.onDocumentedEndpointFoundEvent(mockEvent);

        StepVerifier.create(dynamicRouteLocator.getRoutes())
                .expectNextCount(1)
                .expectComplete()
                .verify();
    }

    private List<EndpointRouteProcessor> getEndpointRouteProcessorMock() {
        return List.of(
                (routeInConstruction, endpoint) -> getRouteBuilderStub()
        );
    }

    private static Route.AsyncBuilder getRouteBuilderStub() {
        return Route.async()
                .id("123")
                .uri("https://example.com")
                .predicate(exchange -> exchange.getRequest()
                        .getPath()
                        .value()
                        .equals("/test-path"));
    }

    @Test
    void testGetRoutes() {
        dynamicRouteLocator = new DynamicRouteLocator(getEndpointRouteProcessorMock());

        DocumentedEndpointFoundEvent mockEvent = mock(DocumentedEndpointFoundEvent.class);

        Assumptions.assumeThatCode(() -> {
            dynamicRouteLocator.onDocumentedEndpointFoundEvent(mockEvent);
            StepVerifier.create(dynamicRouteLocator.getRoutes())
                    .expectNextCount(1)
                    .expectComplete()
                    .verify();
        }).doesNotThrowAnyException();

        StepVerifier.create(dynamicRouteLocator.getRoutes())
                .expectNext(fixPredicate(getRouteBuilderStub()).build())
                .expectComplete()
                .verify();
    }

    /*
     * Route's equals() compares predicates, but AsyncPredicate itself doesn't override equals()
     * hence false negatives when comparing two identical Routes. The method ensures the predicate
     * reference of the passed Route.AsyncBuilder matches the (only) one of the tested DynamicRouteLocator
     */
    @SneakyThrows
    @SuppressWarnings("unchecked")
    private Route.AsyncBuilder fixPredicate(Route.AsyncBuilder routeBuilderStub) {
        Field routesField = dynamicRouteLocator.getClass().getDeclaredField("routes");
        routesField.setAccessible(true);
        Route route = ((Set<Route>) routesField.get(dynamicRouteLocator)).iterator().next();
        return routeBuilderStub.asyncPredicate(route.getPredicate());
    }

    @Test
    void testPredicateEquality() {
        Assertions.assertThat((Predicate<Object>) o -> true).isEqualTo((Predicate<Object>) o -> true);
    }
}