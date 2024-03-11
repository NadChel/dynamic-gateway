package com.example.dynamicgateway.service.endpointCollector;

import com.example.dynamicgateway.events.DiscoverableApplicationFoundEvent;
import com.example.dynamicgateway.events.DiscoverableApplicationLostEvent;
import com.example.dynamicgateway.model.discoverableApplication.DiscoverableApplication;
import com.example.dynamicgateway.model.discoverableApplication.EurekaDiscoverableApplication;
import com.example.dynamicgateway.model.documentedEndpoint.DocumentedEndpoint;
import com.example.dynamicgateway.model.documentedEndpoint.SwaggerEndpoint;
import com.example.dynamicgateway.service.applicationDocClient.ApplicationDocClient;
import com.example.dynamicgateway.service.applicationDocClient.SwaggerClient;
import com.example.dynamicgateway.service.sieve.EndpointSieve;
import com.example.dynamicgateway.testModel.SwaggerEndpointStub;
import com.example.dynamicgateway.testUtil.SwaggerParseResultGenerator;
import com.netflix.discovery.shared.Application;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpMethod;
import org.springframework.util.ReflectionUtils;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

class SwaggerEndpointCollectorTest {
    private SwaggerEndpointCollector collector;

    @Test
    void whenCreated_hasNoEndpoints() {
        collector = getCollectorWithNullFields();
        assertThat(collector.getCollectedEndpoints()).isEmpty();
    }

    private SwaggerEndpointCollector getCollectorWithNullFields() {
        return new SwaggerEndpointCollector(null, null, null);
    }

    @Test
    void doesNotStoreIdenticalEndpoints() {
        collector = getCollectorWithNullFields();

        assumeThat(collector.getCollectedEndpoints()).isEmpty();

        SwaggerEndpoint endpoint = SwaggerEndpointStub.builder()
                .method(HttpMethod.GET)
                .path("/test-path")
                .build();

        SwaggerEndpoint endpointCopy = SwaggerEndpointStub.builder()
                .method(HttpMethod.GET)
                .path("/test-path")
                .build();

        Stream.of(endpoint, endpointCopy).forEach(this::addEndpoint);

        assertThat(collector.getCollectedEndpoints()).hasSize(1);
    }

    @SneakyThrows
    @SuppressWarnings({"unchecked", "DataFlowIssue"})
    private void addEndpoint(SwaggerEndpoint endpoint) {
        Field documentedEndpointsField =
                ReflectionUtils.findField(SwaggerEndpointCollector.class, "documentedEndpoints");
        assumeThat(documentedEndpointsField).isNotNull();
        documentedEndpointsField.setAccessible(true);
        ((Set<SwaggerEndpoint>) documentedEndpointsField.get(collector)).add(endpoint);
    }

    @Test
    void hasEndpoint_withExistingEndpoint_returnsTrue() {
        collector = getCollectorWithNullFields();

        SwaggerEndpoint endpoint = SwaggerEndpointStub.builder()
                .method(HttpMethod.GET)
                .path("/test-path")
                .build();

        HttpMethod method = endpoint.getDetails().getMethod();
        String path = endpoint.getDetails().getPath();
        assertThat(collector.hasEndpoint(method, path)).isFalse();
        addEndpoint(endpoint);
        assertThat(collector.hasEndpoint(method, path)).isTrue();
    }

    @Test
    void hasEndpoint_withNonExistingEndpoint_returnsFalse() {
        collector = getCollectorWithNullFields();

        SwaggerEndpoint endpointToAdd = SwaggerEndpointStub.builder()
                .method(HttpMethod.GET)
                .path("/test-path-one")
                .build();

        SwaggerEndpoint endpointToLeaveOut = SwaggerEndpointStub.builder()
                .method(HttpMethod.GET)
                .path("/test-path-two")
                .build();

        addEndpoint(endpointToAdd);
        HttpMethod endpointToAddMethod = endpointToAdd.getDetails().getMethod();
        String endpointToAddPath = endpointToAdd.getDetails().getPath();
        assumeThat(collector.hasEndpoint(endpointToAddMethod, endpointToAddPath)).isTrue();

        HttpMethod endpointToLeaveOutMethod = endpointToLeaveOut.getDetails().getMethod();
        String endpointToLeaveOutPath = endpointToLeaveOut.getDetails().getPath();
        assertThat(collector.hasEndpoint(endpointToLeaveOutMethod, endpointToLeaveOutPath)).isFalse();
    }

    @Test
    void onDiscoverableApplicationFoundEvent_collectsAllowedApplicationsEndpoints() {
        String appName = "test-application";

        DiscoverableApplication<?> discoverableApplicationMock = mock(DiscoverableApplication.class);
        given(discoverableApplicationMock.getName()).willReturn(appName);

        List<SwaggerEndpoint> endpoints = List.of(
                SwaggerEndpointStub.builder()
                        .declaringAppName(appName)
                        .method(HttpMethod.GET)
                        .path("/test-path-one")
                        .build(),
                SwaggerEndpointStub.builder()
                        .declaringAppName(appName)
                        .method(HttpMethod.POST)
                        .path("/test-path-one")
                        .build(),
                SwaggerEndpointStub.builder()
                        .declaringAppName(appName)
                        .method(HttpMethod.POST)
                        .path("/test-path-two")
                        .build()
        );

        Predicate<DocumentedEndpoint<?>> onlyPost = e -> e.getDetails()
                .getMethod()
                .equals(HttpMethod.POST);

        List<SwaggerEndpoint> postEndpoints = endpoints.stream()
                .filter(onlyPost)
                .toList();

        SwaggerParseResult parseResult = SwaggerParseResultGenerator.createForEndpoints(endpoints);

        ApplicationDocClient<SwaggerParseResult> docClientMock = mock(SwaggerClient.class);
        given(docClientMock.findApplicationDoc(discoverableApplicationMock)).willReturn(Mono.just(parseResult));

        ApplicationEventPublisher eventPublisherMock = mock(ApplicationEventPublisher.class);

        EndpointSieve onlyPostEndpointSieve = onlyPost::test;
        List<EndpointSieve> sieves = List.of(onlyPostEndpointSieve);

        collector = new SwaggerEndpointCollector(docClientMock, sieves, eventPublisherMock);

        assumeThat(collector.getCollectedEndpoints()).isEmpty();

        DiscoverableApplicationFoundEvent appFoundEvent = new DiscoverableApplicationFoundEvent(discoverableApplicationMock, this);
        collector.onDiscoverableApplicationFoundEvent(appFoundEvent);

        assertThat(collector.getCollectedEndpoints()).containsExactlyInAnyOrderElementsOf(postEndpoints);
    }

    @Test
    void onDiscoverableApplicationLostEvent_evictsApplicationsEndpoints() {
        String resilientAppName = "resilient-app";
        List<SwaggerEndpoint> resilientEndpoints = List.of(
                SwaggerEndpointStub.builder()
                        .declaringAppName(resilientAppName)
                        .method(HttpMethod.GET)
                        .path("/test-path-one")
                        .build(),
                SwaggerEndpointStub.builder()
                        .declaringAppName(resilientAppName)
                        .method(HttpMethod.POST)
                        .path("/test-path-one")
                        .build(),
                SwaggerEndpointStub.builder()
                        .declaringAppName(resilientAppName)
                        .method(HttpMethod.PUT)
                        .path("/test-path-two")
                        .build()
        );

        String fragileAppName = "fragile-app";
        List<SwaggerEndpoint> fragileEndpoints = List.of(
                SwaggerEndpointStub.builder()
                        .declaringAppName(fragileAppName)
                        .method(HttpMethod.HEAD)
                        .path("/test-path-one")
                        .build(),
                SwaggerEndpointStub.builder()
                        .declaringAppName(fragileAppName)
                        .method(HttpMethod.OPTIONS)
                        .path("/test-path-one")
                        .build()
        );

        DiscoverableApplication<Application> resilientApp = mock(EurekaDiscoverableApplication.class);
        given(resilientApp.getName()).willReturn(resilientAppName);

        DiscoverableApplication<Application> fragileApp = mock(EurekaDiscoverableApplication.class);
        given(fragileApp.getName()).willReturn(fragileAppName);

        collector = getCollectorWithNullFields();

        Stream.concat(resilientEndpoints.stream(), fragileEndpoints.stream()).forEach(this::addEndpoint);

        DiscoverableApplicationLostEvent appLostEvent = new DiscoverableApplicationLostEvent(fragileApp, this);
        collector.onDiscoverableApplicationLostEvent(appLostEvent);

        Set<SwaggerEndpoint> retainedEndpoints = collector.getCollectedEndpoints();
        assertThat(retainedEndpoints).containsExactlyInAnyOrderElementsOf(resilientEndpoints);
        assertThat(retainedEndpoints).doesNotContainAnyElementsOf(fragileEndpoints);
    }

    @Test
    void onDiscoverableApplicationFoundEvent_ifNoEndpointsFetched_doesntAddOrPublishAnything() {
        DiscoverableApplication<?> discoverableApplicationMock = mock(DiscoverableApplication.class);
        given(discoverableApplicationMock.getName()).willReturn("some-app");

        ApplicationDocClient<SwaggerParseResult> docClientMock = mock(SwaggerClient.class);
        given(docClientMock.findApplicationDoc(discoverableApplicationMock))
                .willReturn(Mono.just(SwaggerParseResultGenerator.empty()));

        ApplicationEventPublisher eventPublisherMock = mock(ApplicationEventPublisher.class);

        collector = new SwaggerEndpointCollector(docClientMock, Collections.emptyList(), eventPublisherMock);

        DiscoverableApplicationFoundEvent eventMock =
                new DiscoverableApplicationFoundEvent(discoverableApplicationMock, this);

        assumeThat(collector.getCollectedEndpoints()).isEmpty();

        collector.onDiscoverableApplicationFoundEvent(eventMock);

        assertThat(collector.getCollectedEndpoints()).isEmpty();
        then(eventPublisherMock).shouldHaveNoInteractions();
    }
}