package com.example.dynamicgateway.service.swaggerUiSupport;

import com.example.dynamicgateway.model.documentedApplication.SwaggerApplication;
import com.example.dynamicgateway.model.documentedEndpoint.DocumentedEndpoint;
import com.example.dynamicgateway.model.documentedEndpoint.SwaggerEndpoint;
import com.example.dynamicgateway.model.gatewayMeta.GatewayMeta;
import com.example.dynamicgateway.model.uiConfig.SwaggerUiConfig;
import com.example.dynamicgateway.service.endpointCollector.EndpointCollector;
import com.example.dynamicgateway.service.endpointCollector.SwaggerEndpointCollector;
import com.example.dynamicgateway.util.Cloner;
import com.example.dynamicgateway.util.EndpointUtil;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.text.MessageFormat;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Component
@Slf4j
public class BasicSwaggerUiSupport implements SwaggerUiSupport {
    private final EndpointCollector<SwaggerEndpoint> endpointCollector;
    private final GatewayMeta gatewayMeta;

    public BasicSwaggerUiSupport(EndpointCollector<SwaggerEndpoint> endpointCollector,
                                 GatewayMeta gatewayMeta) {
        this.endpointCollector = endpointCollector;
        this.gatewayMeta = gatewayMeta;
    }

    /**
     * Returns a {@code Mono} of {@link SwaggerUiConfig} referencing all {@link SwaggerApplication}s
     * declaring at least one {@link DocumentedEndpoint} collected by the injected {@link EndpointCollector}
     */
    @Override
    public Mono<SwaggerUiConfig> getSwaggerUiConfig() {
        return Flux.fromStream(endpointCollector.stream())
                .map(SwaggerEndpoint::getDeclaringApp)
                .collect(Collectors.toSet())
                .map(SwaggerUiConfig::from);
    }

    /**
     * Returns a {@code Mono} of a deep-copy of an {@link OpenAPI} contained in a {@code SwaggerApplication} that:
     * <p>
     * 1. Has at least one endpoint {@link SwaggerEndpointCollector#getCollectedEndpoints() collected} by
     * the injected {@code EndpointCollector} <em>and</em>
     * <p>
     * 2. Has a {@link SwaggerApplication#getName() name} equal to the passed-in string
     * <p>
     * Once a matching {@code SwaggerApplication} is found, its {@code OpenAPI} object is extracted
     * by invoking {@code getNativeDoc().getOpenAPI()} on it
     * <p>
     * After the {@code OpenAPI} object is extracted, the following sequence of operations is performed on it
     * before wrapping it in a returned {@code Mono}:
     * <p>
     * 1. It is sanitized of all endpoints <em>not</em> collected by the injected {@code EndpointCollector}
     * <p>
     * 2. {@link GatewayMeta#getVersionPrefix() Version prefixes} are set in place of all
     * {@link GatewayMeta#getIgnoredPrefixes() ignored prefixes} (or simply appended if an
     * endpoint path doesn't start with any ignored prefix)
     * <p>
     * 3. {@link GatewayMeta#getServers() Servers} are {@link OpenAPI#setServers(List) set}
     * <p>
     * None of the mutations affect the original {@code OpenAPI}
     *
     * @param appName name of the {@code SwaggerApplication} whose {@code OpenAPI} should be mutated and
     *                asynchronously returned
     * @return a {@code Mono} of a matching {@code OpenAPI} or a {@code Mono} of {@code IllegalArgumentException}
     * if no {@code SwaggerApplication} matching the aforementioned conditions was found
     */
    @Override
    public Mono<OpenAPI> getSwaggerAppDoc(String appName) {
        return Flux.fromStream(endpointCollector.stream())
                .map(SwaggerEndpoint::getDeclaringApp)
                .filter(documentedApplication -> documentedApplication.getName().equals(appName))
                .switchIfEmpty(Mono.error(new IllegalArgumentException(MessageFormat.format(
                        "No service with name {0} is known to this Gateway", appName
                ))))
                .next()
                .map(SwaggerApplication::getNativeDoc)
                .map(SwaggerParseResult::getOpenAPI)
                .map(this::deepCopy)
                .doOnNext(this::removeNotCollectedEndpoints)
                .doOnNext(this::setGatewayPrefixes)
                .doOnNext(this::setGatewayServers);
    }

    private OpenAPI deepCopy(OpenAPI openAPI) {
        return Cloner.deepCopy(openAPI, OpenAPI.class);
    }

    private void removeNotCollectedEndpoints(OpenAPI openAPI) {
        Paths newPaths = openAPI
                .getPaths()
                .entrySet()
                .stream()
                .peek(this::removeNotCollectedEndpoints)
                .filter(this::hasAtLeastOneOperationLeft)
                .collect(toPathsCollector());
        openAPI.setPaths(newPaths);
    }

    private void removeNotCollectedEndpoints(Map.Entry<String, PathItem> pathPathItemEntry) {
        pathPathItemEntry.getValue()
                .readOperationsMap()
                .forEach((method, operation) -> removeOperationIfNotCollected(pathPathItemEntry, method));
    }

    private void removeOperationIfNotCollected(Map.Entry<String, PathItem> pathPathItemEntry,
                                               PathItem.HttpMethod method) {
        HttpMethod springMethod = HttpMethod.valueOf(method.toString());
        String path = pathPathItemEntry.getKey();
        if (!endpointCollector.hasEndpoint(springMethod, path))
            removeOperation(pathPathItemEntry, method);
    }

    private void removeOperation(Map.Entry<String, PathItem> pathPathItemEntry,
                                 PathItem.HttpMethod method) {
        pathPathItemEntry.getValue().operation(method, null);
    }

    private boolean hasAtLeastOneOperationLeft(Map.Entry<String, PathItem> pathPathItemEntry) {
        return !pathPathItemEntry.getValue().readOperationsMap().isEmpty();
    }

    private static Collector<Map.Entry<String, PathItem>, ?, Paths> toPathsCollector() {
        return Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (oldVal, newVal) -> newVal,
                Paths::new);
    }

    private void setGatewayPrefixes(OpenAPI openAPI) {
        Paths newPaths = openAPI
                .getPaths()
                .entrySet()
                .stream()
                .map(this::withGatewayPrefixesSet)
                .collect(toPathsCollector());
        openAPI.setPaths(newPaths);
    }

    private Map.Entry<String, PathItem> withGatewayPrefixesSet(Map.Entry<String, PathItem> pathPathItemEntry) {
        String path = pathPathItemEntry.getKey();
        String prefixedPath = withGatewayPrefixSet(path);

        PathItem pathItem = pathPathItemEntry.getValue();
        return new AbstractMap.SimpleEntry<>(prefixedPath, pathItem);
    }

    private String withGatewayPrefixSet(String originalPath) {
        String nonPrefixedPath = EndpointUtil.pathWithRemovedPrefix(originalPath, gatewayMeta);
        return gatewayMeta.getVersionPrefix() + nonPrefixedPath;
    }

    private void setGatewayServers(OpenAPI openAPI) {
        openAPI.setServers(gatewayMeta.getServers());
    }
}
