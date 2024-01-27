package com.example.dynamicgateway.service.endpointCollector;

import com.example.dynamicgateway.client.SwaggerClient;
import com.example.dynamicgateway.model.discoverableApplication.DiscoverableApplication;
import com.example.dynamicgateway.model.discoverableApplication.EurekaDiscoverableApplication;
import com.example.dynamicgateway.model.documentedApplication.SwaggerApplication;
import com.example.dynamicgateway.model.documentedEndpoint.SwaggerEndpoint;
import com.example.dynamicgateway.model.endpointDetails.SwaggerEndpointDetails;
import com.example.dynamicgateway.service.applicationFinder.ApplicationFinder;
import com.example.dynamicgateway.service.applicationFinder.EurekaApplicationFinder;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpMethod;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
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

        SwaggerApplication applicationMock = mock(SwaggerApplication.class);
        when(applicationMock.getName()).thenReturn("test-application");

        SwaggerEndpoint endpointFake = new SwaggerEndpoint(
                applicationMock,
                SwaggerEndpointDetails.builder()
                        .setMethod(PathItem.HttpMethod.GET)
                        .setPath("/test-path")
                        .build()
        );
        SwaggerEndpoint endpointFakeCopy = new SwaggerEndpoint(
                applicationMock,
                SwaggerEndpointDetails.builder()
                        .setMethod(PathItem.HttpMethod.GET)
                        .setPath("/test-path")
                        .build()
        );

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

        SwaggerApplication applicationMock = mock(SwaggerApplication.class);
        when(applicationMock.getName()).thenReturn("test-application");

        SwaggerEndpoint endpointFake = new SwaggerEndpoint(
                applicationMock,
                SwaggerEndpointDetails.builder()
                        .setMethod(PathItem.HttpMethod.GET)
                        .setPath("/test-path")
                        .build()
        );

        HttpMethod testMethod = endpointFake.getDetails().getMethod();
        String testPath = endpointFake.getDetails().getPath();
        assertThat(collector.hasEndpoint(testMethod, testPath)).isFalse();
        addEndpoint(endpointFake);
        assertThat(collector.hasEndpoint(testMethod, testPath)).isTrue();
    }

    @Test
    void testHasEndpoint_withNonExistingEndpoint() {
        collector = getCollectorFake();

        SwaggerApplication applicationMock = mock(SwaggerApplication.class);
        when(applicationMock.getName()).thenReturn("test-application");

        SwaggerEndpoint endpointFakeToAdd = new SwaggerEndpoint(
                applicationMock,
                SwaggerEndpointDetails.builder()
                        .setMethod(PathItem.HttpMethod.GET)
                        .setPath("/test-path-one")
                        .build()
        );
        SwaggerEndpoint endpointFakeToLeaveOut = new SwaggerEndpoint(
                applicationMock,
                SwaggerEndpointDetails.builder()
                        .setMethod(PathItem.HttpMethod.POST)
                        .setPath("/test-path-two")
                        .build()
        );

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
        String testAppName = "test-application";

        DiscoverableApplication discoverableApplicationMock = mock(EurekaDiscoverableApplication.class);
        when(discoverableApplicationMock.getName()).thenReturn(testAppName);

        ApplicationFinder applicationFinderMock = mock(EurekaApplicationFinder.class);
        doReturn(Set.of(discoverableApplicationMock)).when(applicationFinderMock).findOtherRegisteredApplications();

        SwaggerApplication swaggerApplicationMock = mock(SwaggerApplication.class);
        when(swaggerApplicationMock.getName()).thenReturn(testAppName);

        List<SwaggerEndpoint> endpointFakes = List.of(
                new SwaggerEndpoint(
                        swaggerApplicationMock,
                        SwaggerEndpointDetails.builder()
                                .setMethod(PathItem.HttpMethod.GET)
                                .setPath("/test-path-one")
                                .build()
                ),
                new SwaggerEndpoint(
                        swaggerApplicationMock,
                        SwaggerEndpointDetails.builder()
                                .setMethod(PathItem.HttpMethod.POST)
                                .setPath("/test-path-one")
                                .build()
                ),
                new SwaggerEndpoint(
                        swaggerApplicationMock,
                        SwaggerEndpointDetails.builder()
                                .setMethod(PathItem.HttpMethod.PUT)
                                .setPath("/test-path-two")
                                .build()
                )
        );

        SwaggerParseResult swaggerParseResultFake = createSwaggerParseResultFake(endpointFakes);

        SwaggerClient applicationDocClientMock = mock(SwaggerClient.class);
        doReturn(Mono.just(swaggerParseResultFake)).when(applicationDocClientMock).findApplicationDoc(discoverableApplicationMock);

        ApplicationEventPublisher eventPublisherMock = mock(ApplicationEventPublisher.class);

        collector = new SwaggerEndpointCollector(applicationFinderMock, applicationDocClientMock, eventPublisherMock, Collections.emptyList());

        refreshingMethod.accept(collector);

        assertThat(collector.getCollectedEndpoints()).asInstanceOf(COLLECTION).containsExactlyInAnyOrderElementsOf(endpointFakes);
    }

    private SwaggerParseResult createSwaggerParseResultFake(List<SwaggerEndpoint> endpoints) {
        SwaggerParseResult parseResultFake = new SwaggerParseResult();
        OpenAPI openApiFake = createOpenApiFake(endpoints);
        parseResultFake.setOpenAPI(openApiFake);
        return parseResultFake;
    }

    private OpenAPI createOpenApiFake(List<SwaggerEndpoint> endpoints) {
        OpenAPI openApiFake = new OpenAPI();
        openApiFake.setInfo(createInfoFake());
        openApiFake.setPaths(createPathsFake(endpoints));
        return openApiFake;
    }

    private Info createInfoFake() {
        Info infoFake = new Info();
        infoFake.setDescription("Test description");
        return infoFake;
    }

    private Paths createPathsFake(List<SwaggerEndpoint> endpoints) {
        Paths pathsFake = new Paths();
        Map<String, List<SwaggerEndpointDetails>> pathToDetails = createPathToDetailsMap(endpoints);
        for (Map.Entry<String, List<SwaggerEndpointDetails>> pathEntry : pathToDetails.entrySet()) {
            String path = pathEntry.getKey();
            PathItem pathItem = createPathItemFake(pathEntry.getValue());
            pathsFake.addPathItem(path, pathItem);
        }
        return pathsFake;
    }

    private Map<String, List<SwaggerEndpointDetails>> createPathToDetailsMap(List<SwaggerEndpoint> endpoints) {
        return endpoints.stream().collect(Collectors.toMap(
                endpoint -> endpoint.getDetails().getPath(),
                endpoint -> {
                    ArrayList<SwaggerEndpointDetails> details = new ArrayList<>();
                    details.add(endpoint.getDetails());
                    return details;
                },
                (oldDetailsList, newDetailsList) -> {
                    oldDetailsList.add(newDetailsList.get(0));
                    return oldDetailsList;
                }
        ));
    }

    private PathItem createPathItemFake(List<SwaggerEndpointDetails> detailsForPath) {
        PathItem pathItem = new PathItem();
        for (SwaggerEndpointDetails detail : detailsForPath) {
            PathItem.HttpMethod method = Arrays.stream(PathItem.HttpMethod.values())
                    .filter(m -> m.name().equals(detail.getMethod().name()))
                    .findFirst()
                    .orElseThrow();
            Operation operation = new Operation();
            pathItem.operation(method, operation);
        }
        return pathItem;
    }

    @Test
    void onCacheRefreshedEvent() {
        testEndpointRefreshingMethod(SwaggerEndpointCollector::onCacheRefreshedEvent);
    }
}