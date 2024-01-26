package com.example.dynamicgateway.service.routeLocator;

import com.example.dynamicgateway.events.DocumentedEndpointFoundEvent;
import com.example.dynamicgateway.service.routeLocator.util.PathOnlyAsyncPredicate;
import com.example.dynamicgateway.service.routeProcessor.EndpointRouteProcessor;
import lombok.SneakyThrows;
import org.assertj.core.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.route.Route;
import reactor.test.StepVerifier;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;
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
    void testOnDocumentedEndpointFoundEvent_withOneEvent() {
        dynamicRouteLocator = new DynamicRouteLocator(getEndpointRouteProcessorStub());

        DocumentedEndpointFoundEvent mockEvent = mock(DocumentedEndpointFoundEvent.class);

        dynamicRouteLocator.onDocumentedEndpointFoundEvent(mockEvent);

        assertThat(getRouteSet().size()).isEqualTo(1);
    }

    private List<EndpointRouteProcessor> getEndpointRouteProcessorStub() {
        return List.of(
                (routeInConstruction, endpoint) -> getRouteBuilderStub()
        );
    }

    private static Route.AsyncBuilder getRouteBuilderStub() {
        return Route.async()
                .id("123")
                .uri("https://example.com")
                .asyncPredicate(PathOnlyAsyncPredicate.from("/test-path"));
    }

    @Test
    void testOnDocumentedEndpointFoundEvent_withTwoIdenticalEvents() {
        dynamicRouteLocator = new DynamicRouteLocator(getEndpointRouteProcessorStub());

        DocumentedEndpointFoundEvent mockEvent = mock(DocumentedEndpointFoundEvent.class);
        DocumentedEndpointFoundEvent mockEventCopy = mock(DocumentedEndpointFoundEvent.class);

        dynamicRouteLocator.onDocumentedEndpointFoundEvent(mockEvent);
        dynamicRouteLocator.onDocumentedEndpointFoundEvent(mockEventCopy);

        assertThat(getRouteSet().size()).isEqualTo(1);
    }

    @SneakyThrows
    private Set<?> getRouteSet() {
        Field routesField =  dynamicRouteLocator.getClass()
                .getDeclaredField("routes");
        routesField.setAccessible(true);
        return (Set<?>) routesField.get(dynamicRouteLocator);
    }

    @Test
    void ifOneEventFired_returnsFluxOfOneRoute() {
        dynamicRouteLocator = new DynamicRouteLocator(getEndpointRouteProcessorStub());

        DocumentedEndpointFoundEvent mockEvent = mock(DocumentedEndpointFoundEvent.class);

        dynamicRouteLocator.onDocumentedEndpointFoundEvent(mockEvent);

        assumeThat(getRouteSet().size()).isEqualTo(1);

        StepVerifier.create(dynamicRouteLocator.getRoutes())
                .expectNextCount(1)
                .expectComplete()
                .verify();
    }

    @Test
    void testGetRoutes() {
        dynamicRouteLocator = new DynamicRouteLocator(getEndpointRouteProcessorStub());

        DocumentedEndpointFoundEvent mockEvent = mock(DocumentedEndpointFoundEvent.class);

        Assumptions.assumeThatCode(() -> {
            dynamicRouteLocator.onDocumentedEndpointFoundEvent(mockEvent);
            StepVerifier.create(dynamicRouteLocator.getRoutes())
                    .expectNextCount(1)
                    .expectComplete()
                    .verify();
        }).doesNotThrowAnyException();

        StepVerifier.create(dynamicRouteLocator.getRoutes())
                .expectNext(getRouteBuilderStub().build())
                .expectComplete()
                .verify();
    }
}