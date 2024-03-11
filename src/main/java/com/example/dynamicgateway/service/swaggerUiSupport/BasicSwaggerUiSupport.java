package com.example.dynamicgateway.service.swaggerUiSupport;

import com.example.dynamicgateway.model.documentedApplication.SwaggerApplication;
import com.example.dynamicgateway.model.documentedEndpoint.SwaggerEndpoint;
import com.example.dynamicgateway.model.gatewayMeta.GatewayMeta;
import com.example.dynamicgateway.model.uiConfig.SwaggerUiConfig;
import com.example.dynamicgateway.service.endpointCollector.EndpointCollector;
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
import java.util.Map;
import java.util.NoSuchElementException;
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

    @Override
    public Mono<SwaggerUiConfig> getSwaggerUiConfig() {
        return Flux.fromStream(endpointCollector.stream())
                .map(SwaggerEndpoint::getDeclaringApp)
                .collect(Collectors.toSet())
                .map(SwaggerUiConfig::from);
    }

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
                .doOnNext(this::removeIgnoredEndpoints)
                .doOnNext(this::setGatewayPrefixes)
                .doOnNext(this::setGatewayServers);
    }

    private OpenAPI deepCopy(OpenAPI openAPI) {
        return Cloner.deepCopy(openAPI, OpenAPI.class);
    }

    private void removeIgnoredEndpoints(OpenAPI openAPI) {
        Paths newPaths = openAPI.getPaths().entrySet().stream()
                .peek(pathPathItemEntry -> pathPathItemEntry.getValue()
                        .readOperationsMap()
                        .forEach((method, operation) -> {
                            HttpMethod springMethod = HttpMethod.valueOf(method.toString());
                            String path = pathPathItemEntry.getKey();
                            if (!endpointCollector.hasEndpoint(springMethod, path)) {
                                pathPathItemEntry.getValue().operation(method, null);
                            }
                        }))
                .filter(pathPathItemEntry -> !pathPathItemEntry.getValue().readOperationsMap().isEmpty())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldVal, newVal) -> newVal, Paths::new));
        openAPI.setPaths(newPaths);
    }

    private void setGatewayPrefixes(OpenAPI openAPI) {
        Paths newPaths = new Paths();
        for (Map.Entry<String, PathItem> pathItemEntry : openAPI.getPaths().entrySet()) {
            String servicePath = pathItemEntry.getKey();
            PathItem pathItem = pathItemEntry.getValue();

            String nonPrefixedPath = endpointCollector.stream()
                    .filter(documentedEndpoint -> documentedEndpoint.getDetails().getPath().equals(servicePath))
                    .map(documentedEndpoint -> EndpointUtil.withRemovedPrefix(documentedEndpoint, gatewayMeta))
                    .findFirst()
                    .orElseThrow(() -> new NoSuchElementException(MessageFormat.format(
                            "No endpoint found. Requested path: {0}", servicePath
                    )));

            String prefixedPath = gatewayMeta.getVersionPrefix() + nonPrefixedPath;
            newPaths.put(prefixedPath, pathItem);
        }
        openAPI.setPaths(newPaths);
    }

    private void setGatewayServers(OpenAPI openAPI) {
        openAPI.setServers(gatewayMeta.getServers());
    }
}
