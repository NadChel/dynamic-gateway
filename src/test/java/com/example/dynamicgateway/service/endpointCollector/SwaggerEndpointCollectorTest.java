package com.example.dynamicgateway.service.endpointCollector;

import com.example.dynamicgateway.model.discoverableApplication.DiscoverableApplication;
import com.example.dynamicgateway.model.discoverableApplication.EurekaDiscoverableApplication;
import com.example.dynamicgateway.model.documentedEndpoint.SwaggerEndpoint;
import com.example.dynamicgateway.service.applicationDocClient.SwaggerClient;
import com.example.dynamicgateway.service.applicationFinder.ApplicationFinder;
import com.example.dynamicgateway.service.applicationFinder.EurekaApplicationFinder;
import com.example.dynamicgateway.testModel.SwaggerEndpointStub;
import com.example.dynamicgateway.testUtil.SwaggerParseResultGenerator;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpMethod;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;
import static org.assertj.core.api.InstanceOfAssertFactories.COLLECTION;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SwaggerEndpointCollectorTest {
    private SwaggerEndpointCollector collector;

    @Test
    void whenCreated_hasNoEndpoints() {
        collector = getCollectorFake();
        assertThat(collector.getCollectedEndpoints()).asInstanceOf(COLLECTION).isEmpty();
    }

    private SwaggerEndpointCollector getCollectorFake() {
        return new SwaggerEndpointCollector(null, null, null, null);
    }

    @Test
    void doesNotStoreIdenticalEndpoints() {
        collector = getCollectorFake();

        assumeThat(collector.getCollectedEndpoints()).asInstanceOf(COLLECTION).isEmpty();

        SwaggerEndpoint endpointFake = SwaggerEndpointStub.builder()
                .method(PathItem.HttpMethod.GET)
                .path("/test-path")
                .build();

        SwaggerEndpoint endpointFakeCopy = SwaggerEndpointStub.builder()
                .method(PathItem.HttpMethod.GET)
                .path("/test-path")
                .build();

        Stream.of(endpointFake, endpointFakeCopy).forEach(this::addEndpoint);

        assertThat(collector.getCollectedEndpoints()).asInstanceOf(COLLECTION).hasSize(1);
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    private void addEndpoint(SwaggerEndpoint endpointFake) {
        Field documentedEndpointsField = collector.getClass().getDeclaredField("documentedEndpoints");
        documentedEndpointsField.setAccessible(true);
        ((Set<SwaggerEndpoint>) documentedEndpointsField.get(collector)).add(endpointFake);
    }

    @Test
    void testHasEndpoint_withExistingEndpoint() {
        collector = getCollectorFake();

        SwaggerEndpoint endpointFake = SwaggerEndpointStub.builder()
                .method(PathItem.HttpMethod.GET)
                .path("/test-path")
                .build();

        HttpMethod testMethod = endpointFake.getDetails().getMethod();
        String testPath = endpointFake.getDetails().getPath();
        assertThat(collector.hasEndpoint(testMethod, testPath)).isFalse();
        addEndpoint(endpointFake);
        assertThat(collector.hasEndpoint(testMethod, testPath)).isTrue();
    }

    @Test
    void testHasEndpoint_withNonExistingEndpoint() {
        collector = getCollectorFake();

        SwaggerEndpoint endpointFakeToAdd = SwaggerEndpointStub.builder()
                .method(PathItem.HttpMethod.GET)
                .path("/test-path-one")
                .build();

        SwaggerEndpoint endpointFakeToLeaveOut = SwaggerEndpointStub.builder()
                .method(PathItem.HttpMethod.POST)
                .path("/test-path-two")
                .build();

        addEndpoint(endpointFakeToAdd);
        HttpMethod endpointFakeToAddMethod = endpointFakeToAdd.getDetails().getMethod();
        String endpointFakeToAddPath = endpointFakeToAdd.getDetails().getPath();
        assumeThat(collector.hasEndpoint(endpointFakeToAddMethod, endpointFakeToAddPath)).isTrue();

        HttpMethod endpointFakeToLeaveOutMethod = endpointFakeToLeaveOut.getDetails().getMethod();
        String endpointFakeToLeaveOutPath = endpointFakeToLeaveOut.getDetails().getPath();
        assertThat(collector.hasEndpoint(endpointFakeToLeaveOutMethod, endpointFakeToLeaveOutPath)).isFalse();
    }

    @Test
    void testOnApplicationReadyEvent() {
        testEndpointRefreshingMethod(SwaggerEndpointCollector::onApplicationReadyEvent);
    }

    private void testEndpointRefreshingMethod(Consumer<SwaggerEndpointCollector> refreshingMethod) {
        DiscoverableApplication discoverableApplicationMock = mock(EurekaDiscoverableApplication.class);
        when(discoverableApplicationMock.getName()).thenReturn("test-application");

        ApplicationFinder applicationFinderMock = mock(EurekaApplicationFinder.class);
        doReturn(Set.of(discoverableApplicationMock)).when(applicationFinderMock).findOtherRegisteredApplications();

        List<SwaggerEndpoint> endpointFakes = List.of(
                SwaggerEndpointStub.builder()
                        .method(PathItem.HttpMethod.GET)
                        .path("/test-path-one")
                        .build(),
                SwaggerEndpointStub.builder()
                        .method(PathItem.HttpMethod.POST)
                        .path("/test-path-one")
                        .build(),
                SwaggerEndpointStub.builder()
                        .method(PathItem.HttpMethod.PUT)
                        .path("/test-path-two")
                        .build()
        );

        SwaggerParseResultGenerator parseResultGenerator = new SwaggerParseResultGenerator();
        SwaggerParseResult swaggerParseResultFake = parseResultGenerator.createForEndpoints(endpointFakes);

        SwaggerClient applicationDocClientMock = mock(SwaggerClient.class);
        doReturn(Mono.just(swaggerParseResultFake)).when(applicationDocClientMock).findApplicationDoc(discoverableApplicationMock);

        ApplicationEventPublisher eventPublisherMock = mock(ApplicationEventPublisher.class);

        collector = new SwaggerEndpointCollector(applicationFinderMock, applicationDocClientMock, eventPublisherMock, Collections.emptyList());

        refreshingMethod.accept(collector);

        assertThat(collector.getCollectedEndpoints()).asInstanceOf(COLLECTION).containsExactlyInAnyOrderElementsOf(endpointFakes);
    }

    @Test
    void onCacheRefreshedEvent() {
        testEndpointRefreshingMethod(SwaggerEndpointCollector::onCacheRefreshedEvent);
    }
}