package com.example.dynamicgateway.service.swaggerUiSupport;

import com.example.dynamicgateway.model.discoverableApplication.DiscoverableApplication;
import com.example.dynamicgateway.model.discoverableApplication.EurekaDiscoverableApplication;
import com.example.dynamicgateway.model.documentedApplication.DocumentedApplication;
import com.example.dynamicgateway.model.documentedApplication.SwaggerApplication;
import com.example.dynamicgateway.model.documentedEndpoint.SwaggerEndpoint;
import com.example.dynamicgateway.model.endpointDetails.SwaggerEndpointDetails;
import com.example.dynamicgateway.model.gatewayMeta.GatewayMeta;
import com.example.dynamicgateway.model.uiConfig.SwaggerUiConfig;
import com.example.dynamicgateway.service.endpointCollector.EndpointCollector;
import com.example.dynamicgateway.service.endpointCollector.SwaggerEndpointCollector;
import com.example.dynamicgateway.testModel.SwaggerEndpointStub;
import com.example.dynamicgateway.testUtil.SwaggerParseResultGenerator;
import com.example.dynamicgateway.util.EndpointUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.discovery.shared.Application;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

class BasicSwaggerUiSupportTest {
    private final List<String> testAppNames = List.of("test-app", "another-test-app", "this-is-app-too");

    @SuppressWarnings("unchecked")
    @Test
    void getSwaggerUiConfig() {
        Set<SwaggerEndpoint> testEndpoints = testAppNames.stream()
                .map(this::appNameToEndpointStub).collect(Collectors.toSet());

        EndpointCollector<SwaggerEndpoint> endpointCollectorMock = spy(EndpointCollector.class);
        when(endpointCollectorMock.getCollectedEndpoints()).thenReturn(testEndpoints);

        BasicSwaggerUiSupport uiSupport = new BasicSwaggerUiSupport(
                endpointCollectorMock, null, null);

        StepVerifier.create(uiSupport.getSwaggerUiConfig())
                .expectNextMatches(this::isExpectedCollectionOfApps)
                .expectComplete()
                .verify();
    }

    private SwaggerEndpointStub appNameToEndpointStub(String name) {
        return SwaggerEndpointStub.builder().declaringAppName(name).build();
    }

    private boolean isExpectedCollectionOfApps(SwaggerUiConfig swaggerUiConfig) {
        return hasExpectedSize(swaggerUiConfig) && hasExpectedContent(swaggerUiConfig);
    }

    private boolean hasExpectedSize(SwaggerUiConfig swaggerUiConfig) {
        return swaggerUiConfig.getSwaggerApplications().size() == testAppNames.size();
    }

    private boolean hasExpectedContent(SwaggerUiConfig swaggerUiConfig) {
        return swaggerUiConfig.getSwaggerApplications()
                .stream()
                .map(DocumentedApplication::getName)
                .allMatch(testAppNames::contains);
    }

    @SneakyThrows
    @Test
    void testGetSwaggerAppDoc() {
        String testAppName = "test-app";

        DiscoverableApplication<Application> discoverableApplicationMock = mock(EurekaDiscoverableApplication.class);
        when(discoverableApplicationMock.getName()).thenReturn(testAppName);

        SwaggerParseResult testParseResult = SwaggerParseResultGenerator.createForEndpoints(
                SwaggerEndpointStub.builder().method(HttpMethod.POST).path("/auth/test-path").build(),
                SwaggerEndpointStub.builder().method(HttpMethod.DELETE).path("/test-path").build(),
                SwaggerEndpointStub.builder().method(HttpMethod.GET).path("/test/path").build()
        );

        SwaggerApplication testSwaggerApplication = new SwaggerApplication(discoverableApplicationMock, testParseResult);

        EndpointCollector<SwaggerEndpoint> testEndpointCollector = new SwaggerEndpointCollector(null, null, null);
        ReflectionTestUtils.setField(testEndpointCollector, "documentedEndpoints", Set.copyOf(testSwaggerApplication.getEndpoints()));

        GatewayMeta gatewayMetaMock = mock(GatewayMeta.class);
        when(gatewayMetaMock.getVersionPrefix()).thenReturn("/test-api/v0");
        when(gatewayMetaMock.getIgnoredPrefixes()).thenReturn(List.of("/auth"));
        when(gatewayMetaMock.getServers()).thenReturn(List.of(
                new Server().url("https://localhost:1234").description("Server One"),
                new Server().url("https://localhost:4321").description("Server Two")
        ));

        ObjectMapper objectMapper = new ObjectMapper();

        BasicSwaggerUiSupport uiSupport = new BasicSwaggerUiSupport(
                testEndpointCollector, gatewayMetaMock, objectMapper);

        String parseResultSnapshot = objectMapper.writeValueAsString(testParseResult);

        StepVerifier.create(uiSupport.getSwaggerAppDoc(testAppName))
                .expectNextMatches(openAPI ->
                        isOpenApiCorrect(openAPI, testSwaggerApplication, testEndpointCollector, gatewayMetaMock))
                .expectComplete()
                .verify();

        String parseResultAfterDocForSwaggerUiWasGenerated = objectMapper.writeValueAsString(testParseResult);
        assertThat(parseResultAfterDocForSwaggerUiWasGenerated).isEqualTo(parseResultSnapshot);
    }

    private boolean isOpenApiCorrect(OpenAPI openAPI, SwaggerApplication swaggerApplication,
                                     EndpointCollector<SwaggerEndpoint> endpointCollector, GatewayMeta gatewayMeta) {
        return isApiMetaCorrect(openAPI, gatewayMeta) &&
                areEndpointsCorrect(openAPI, swaggerApplication, endpointCollector, gatewayMeta);
    }

    private boolean isApiMetaCorrect(OpenAPI openAPI, GatewayMeta gatewayMetaMock) {
        return openAPI.getServers().size() == gatewayMetaMock.getServers().size() &&
                openAPI.getServers().containsAll(gatewayMetaMock.getServers());
    }

    private boolean areEndpointsCorrect(OpenAPI openAPI, SwaggerApplication swaggerApplication,
                                        EndpointCollector<SwaggerEndpoint> endpointCollector, GatewayMeta gatewayMeta) {
        return openAPI.getPaths().entrySet().stream().allMatch(pathPathItemEntry ->
                gatewayPrefixesSet(pathPathItemEntry.getKey(), swaggerApplication, gatewayMeta) &&
                        containsOnlyCollectedEndpoints(pathPathItemEntry, endpointCollector, gatewayMeta)
        );
    }

    private boolean gatewayPrefixesSet(String path, SwaggerApplication swaggerApplication, GatewayMeta gatewayMeta) {
        return swaggerApplication.getEndpoints()
                .stream()
                .map(SwaggerEndpoint::getDetails)
                .map(SwaggerEndpointDetails::getPath)
                .map(p -> EndpointUtil.withRemovedPrefix(p, gatewayMeta.getIgnoredPrefixes()))
                .anyMatch(nonPrefixedPath -> path.equals(gatewayMeta.getVersionPrefix() + nonPrefixedPath));
    }

    private boolean containsOnlyCollectedEndpoints(Map.Entry<String, PathItem> pathPathItemEntry,
                                                   EndpointCollector<SwaggerEndpoint> endpointCollector,
                                                   GatewayMeta gatewayMeta) {
        for (PathItem.HttpMethod method : pathPathItemEntry.getValue().readOperationsMap().keySet()) {
            HttpMethod openApiMethod = HttpMethod.valueOf(method.name());
            String openApiUnprefixedPath = unprefixPath(pathPathItemEntry.getKey(), gatewayMeta.getVersionPrefix());

            boolean mismatch = endpointCollector.stream().noneMatch(collectorEndpoint ->
                    collectorEndpoint.getDetails().getMethod().equals(openApiMethod) &&
                            EndpointUtil.withRemovedPrefix(collectorEndpoint.getDetails().getPath(),
                                    gatewayMeta.getIgnoredPrefixes()).equals(openApiUnprefixedPath));

            if (mismatch) return false;
        }
        return true;
    }

    private String unprefixPath(String path, String prefix) {
        return path.substring(prefix.length());
    }
}