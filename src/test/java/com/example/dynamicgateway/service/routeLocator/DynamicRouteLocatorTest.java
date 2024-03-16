package com.example.dynamicgateway.service.routeLocator;

import com.example.dynamicgateway.events.DiscoverableApplicationLostEvent;
import com.example.dynamicgateway.events.DocumentedEndpointFoundEvent;
import com.example.dynamicgateway.model.discoverableApplication.DiscoverableApplication;
import com.example.dynamicgateway.model.documentedEndpoint.DocumentedEndpoint;
import com.example.dynamicgateway.service.routeLocator.util.PathOnlyAsyncPredicate;
import com.example.dynamicgateway.service.routeProcessor.EndpointRouteAssembler;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.route.Route;
import reactor.test.StepVerifier;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;
import static org.assertj.core.api.Assumptions.assumeThatCode;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

class DynamicRouteLocatorTest {
    private DynamicRouteLocator dynamicRouteLocator;

    @Test
    void ifNoEventsFired_hasNoEndpoints() {
        dynamicRouteLocator = new DynamicRouteLocator(Collections.emptyList());

        assertNoRoutes();
    }

    private void assertNoRoutes() {
        assertRouteCount(0);
    }

    private void assertRouteCount(int expectedNumberOfEndpoints) {
        assertThat(getRouteSet()).hasSize(expectedNumberOfEndpoints);
        StepVerifier.create(dynamicRouteLocator.getRoutes())
                .expectNextCount(expectedNumberOfEndpoints)
                .verifyComplete();
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    private Set<Route> getRouteSet() {
        Field routesField = dynamicRouteLocator.getClass()
                .getDeclaredField("routes");
        assumeThat(routesField).isNotNull();
        routesField.setAccessible(true);
        return (Set<Route>) routesField.get(dynamicRouteLocator);
    }

    @Test
    void onNullDocumentedEndpointFoundEvent_stillHasNoEndpoints() {
        dynamicRouteLocator = new DynamicRouteLocator(Collections.emptyList());

        assumeNoRoutes();

        dynamicRouteLocator.onDocumentedEndpointFoundEvent(null);

        assertNoRoutes();
    }

    private void assumeNoRoutes() {
        assumeThatCode(this::assertNoRoutes).doesNotThrowAnyException();
    }

    @Test
    void onNonNullDocumentedEndpointFoundEvent_whichHasNullEndpoint_locatorStillHasNoEndpoints() {
        dynamicRouteLocator = new DynamicRouteLocator(Collections.emptyList());

        DocumentedEndpointFoundEvent eventMock = mock(DocumentedEndpointFoundEvent.class);
        given(eventMock.getFoundEndpoint()).willReturn(null);

        assumeNoRoutes();

        dynamicRouteLocator.onDocumentedEndpointFoundEvent(eventMock);

        assertNoRoutes();
    }

    @Test
    void onNonNullDocumentedEndpointFoundEvent_withNonNullEndpoint_buildsRoute() {
        dynamicRouteLocator = new DynamicRouteLocator(getEndpointRouteProcessorStub());

        DocumentedEndpoint<?> endpointMock = mock(DocumentedEndpoint.class);

        DocumentedEndpointFoundEvent eventMock = mock(DocumentedEndpointFoundEvent.class);
        given(eventMock.getFoundEndpoint()).willAnswer(i -> endpointMock);

        assumeNoRoutes();

        dynamicRouteLocator.onDocumentedEndpointFoundEvent(eventMock);

        assertOneRoute();
    }

    private static List<EndpointRouteAssembler> getEndpointRouteProcessorStub() {
        return List.of(
                (routeInConstruction, endpoint) -> getRouteBuilderStub()
        );
    }

    private static Route.AsyncBuilder getRouteBuilderStub() {
        return getLegalRouteBuilderWithUri("https://example.com");
    }

    private static Route.AsyncBuilder getLegalRouteBuilderWithUri(String uri) {
        return Route.async()
                .id(String.valueOf(uri.hashCode()))
                .uri(uri)
                .asyncPredicate(PathOnlyAsyncPredicate.fromPath("/"));
    }

    private void assertOneRoute() {
        assertRouteCount(1);
    }

    @Test
    void ifSameEndpointFoundTwice_buildsOnlyOneRoute() {
        dynamicRouteLocator = new DynamicRouteLocator(getEndpointRouteProcessorStub());

        DocumentedEndpoint<?> documentedEndpointMock = mock(DocumentedEndpoint.class);

        DocumentedEndpointFoundEvent eventMock = mock(DocumentedEndpointFoundEvent.class);
        DocumentedEndpointFoundEvent eventMockCopy = mock(DocumentedEndpointFoundEvent.class);

        given(eventMock.getFoundEndpoint()).willAnswer(i -> documentedEndpointMock);
        given(eventMockCopy.getFoundEndpoint()).willAnswer(i -> documentedEndpointMock);

        assumeNoRoutes();

        dynamicRouteLocator.onDocumentedEndpointFoundEvent(eventMock);
        dynamicRouteLocator.onDocumentedEndpointFoundEvent(eventMockCopy);

        assertOneRoute();
    }

    @Test
    void onDocumentedEndpointFoundEvent_buildsRouteMatchingExpectedParameters() {
        dynamicRouteLocator = new DynamicRouteLocator(getEndpointRouteProcessorStub());

        DocumentedEndpoint<?> endpointMock = mock(DocumentedEndpoint.class);

        DocumentedEndpointFoundEvent eventMock = mock(DocumentedEndpointFoundEvent.class);
        given(eventMock.getFoundEndpoint()).willAnswer(i -> endpointMock);

        assumeNoRoutes();

        dynamicRouteLocator.onDocumentedEndpointFoundEvent(eventMock);

        assumeOneRoute();

        assertOnlyRoute(getRouteBuilderStub().build());
    }

    private void assumeOneRoute() {
        assumeThatCode(this::assertOneRoute).doesNotThrowAnyException();
    }

    private void assertOnlyRoute(Route onlyExpectedRoute) {
        assertOnlyRoutes(onlyExpectedRoute);
    }

    private void assertOnlyRoutes(Route... onlyExpectedRoutes) {
        assertThat(getRouteSet()).containsExactlyInAnyOrder(onlyExpectedRoutes);
        StepVerifier.create(dynamicRouteLocator.getRoutes().collectList())
                .assertNext(routes -> assertThat(routes).containsExactlyInAnyOrder(onlyExpectedRoutes))
                .verifyComplete();
    }

    @Test
    void onDiscoverableApplicationLostEvent_removesAssociatedRoutes() {
        String scheme = "scheme://";

        dynamicRouteLocator = new DynamicRouteLocator(List.of(
                (routeInConstruction, endpoint) -> getLegalRouteBuilderWithUri(scheme + endpoint.getDeclaringApp().getName())
        ));

        String appName = "some-app";
        String anotherAppName = "some-other-app";

        DocumentedEndpoint<?> endpointMock = mock(DocumentedEndpoint.class, RETURNS_DEEP_STUBS);
        given(endpointMock.getDeclaringApp().getName()).willReturn(appName);

        DocumentedEndpoint<?> anotherEndpointMock = mock(DocumentedEndpoint.class, RETURNS_DEEP_STUBS);
        given(anotherEndpointMock.getDeclaringApp().getName()).willReturn(anotherAppName);

        DocumentedEndpointFoundEvent endpointFoundEventMock = mock(DocumentedEndpointFoundEvent.class);
        given(endpointFoundEventMock.getFoundEndpoint()).willAnswer(i -> endpointMock);

        DocumentedEndpointFoundEvent anotherEndpointFoundEventMock = mock(DocumentedEndpointFoundEvent.class);
        given(anotherEndpointFoundEventMock.getFoundEndpoint()).willAnswer(i -> anotherEndpointMock);

        assumeNoRoutes();

        dynamicRouteLocator.onDocumentedEndpointFoundEvent(endpointFoundEventMock);
        dynamicRouteLocator.onDocumentedEndpointFoundEvent(anotherEndpointFoundEventMock);

        assumeOnlyRoutes(
                getLegalRouteBuilderWithUri(scheme + appName).build(),
                getLegalRouteBuilderWithUri(scheme + anotherAppName).build()
        );

        DiscoverableApplication<?> appMock = mock(DiscoverableApplication.class);
        given(appMock.getName()).willReturn(appName);

        DiscoverableApplicationLostEvent eventMock = mock(DiscoverableApplicationLostEvent.class);
        given(eventMock.getLostApp()).willAnswer(i -> appMock);

        dynamicRouteLocator.onDiscoverableApplicationLostEvent(eventMock);

        assertOnlyRoute(getLegalRouteBuilderWithUri(scheme + anotherAppName).build());
    }

    private void assumeOnlyRoutes(Route... expectedRoutes) {
        assumeThatCode(() -> assertOnlyRoutes(expectedRoutes)).doesNotThrowAnyException();
    }
}