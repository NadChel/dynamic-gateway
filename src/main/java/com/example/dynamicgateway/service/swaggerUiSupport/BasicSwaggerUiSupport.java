package com.example.dynamicgateway.service.swaggerUiSupport;

import com.example.dynamicgateway.model.documentedApplication.DocumentedApplication;
import com.example.dynamicgateway.model.documentedApplication.SwaggerApplication;
import com.example.dynamicgateway.model.documentedEndpoint.DocumentedEndpoint;
import com.example.dynamicgateway.model.documentedEndpoint.SwaggerEndpoint;
import com.example.dynamicgateway.model.gatewayMeta.GatewayMeta;
import com.example.dynamicgateway.model.uiConfig.SwaggerUiConfig;
import com.example.dynamicgateway.service.endpointCollector.EndpointCollector;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.text.MessageFormat;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class BasicSwaggerUiSupport implements SwaggerUiSupport {
    private final EndpointCollector<SwaggerEndpoint> endpointCollector;
    private final GatewayMeta gatewayMeta;

    @Override
    public Mono<SwaggerUiConfig> getSwaggerUiConfig() {
        Set<SwaggerApplication> swaggerApps = endpointCollector.getKnownEndpoints().stream()
                .map(DocumentedEndpoint::getDeclaringApp)
                .collect(Collectors.toSet());
        return Mono.just(SwaggerUiConfig.from(swaggerApps));
    }

    @Override
    @SneakyThrows
    public Mono<OpenAPI> getSwaggerAppDoc(String appName) {
        return Mono.just(
                endpointCollector.getKnownEndpoints().stream()
                        .map(DocumentedEndpoint::getDeclaringApp)
                        .filter(documentedApplication -> documentedApplication.getName().equals(appName))
                        .map(DocumentedApplication::getNativeDoc)
                        .map(SwaggerParseResult::getOpenAPI)
                        .peek(this::setGatewayPrefixes)
                        .peek(this::setGatewayServers)
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException(MessageFormat.format(
                                "No service with name {0} is known to this Gateway", appName
                        )))
        );
    }

    private void setGatewayPrefixes(OpenAPI openAPI) {
        Paths newPaths = new Paths();
        for (Map.Entry<String, PathItem> pathItemEntry : openAPI.getPaths().entrySet()) {
            String servicePath = pathItemEntry.getKey();
            PathItem pathItem = pathItemEntry.getValue();

            String prefixedPath = servicePath;
            if (servicePath != null && !servicePath.startsWith(gatewayMeta.v1Prefix())) {
                String nonprefixedPath = endpointCollector.getKnownEndpoints().stream()
                        .filter(documentedEndpoint -> documentedEndpoint.getDetails().getPath().equals(servicePath))
                        .map(documentedEndpoint -> documentedEndpoint.getDetails().getNonPrefixedPath())
                        .findFirst()
                        .orElseThrow(() -> new NoSuchElementException(MessageFormat.format(
                                "No endpoint found. Requested path: {0}", servicePath
                        )));

                prefixedPath = gatewayMeta.v1Prefix() + nonprefixedPath;
            }
            newPaths.put(prefixedPath, pathItem);
        }
        openAPI.setPaths(newPaths);
    }

    private void setGatewayServers(OpenAPI openAPI) {
        openAPI.setServers(gatewayMeta.servers());
    }
}
