package by.afinny.apigateway.service.swaggerUiSupport;

import by.afinny.apigateway.model.documentedEndpoint.SwaggerEndpoint;
import by.afinny.apigateway.model.gatewayMeta.GatewayMeta;
import by.afinny.apigateway.model.documentedApplication.DocumentedApplication;
import by.afinny.apigateway.model.documentedApplication.SwaggerApplication;
import by.afinny.apigateway.model.documentedEndpoint.DocumentedEndpoint;
import by.afinny.apigateway.model.uiConfig.SwaggerUiConfig;
import by.afinny.apigateway.service.endpointCollector.EndpointCollector;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.tags.Tag;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.text.MessageFormat;
import java.util.List;
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
                        .peek(this::removeAuthTags)
                        .peek(this::setGatewayPrefixes)
                        .peek(this::setGatewayServers)
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException(MessageFormat.format(
                                "No service with name {0} is known to this Gateway", appName
                        )))
        );
    }

    private void removeAuthTags(OpenAPI openAPI) {
        removeAuthTagsFromApi(openAPI);
        removeAuthTagsFromOperations(openAPI);
    }

    private void removeAuthTagsFromApi(OpenAPI openAPI) {
        if (openAPI.getTags() != null) {
            List<Tag> tagsWithoutAuthTags = openAPI.getTags().stream()
                    .filter(tag -> !isAuthTag(tag))
                    .toList();
            openAPI.setTags(tagsWithoutAuthTags);
        }
    }

    private void removeAuthTagsFromOperations(OpenAPI openAPI) {
        openAPI.getPaths().forEach(
                (path, pathItem) -> pathItem.readOperationsMap()
                        .forEach((method, operation) -> {
                            if (operation.getTags() != null) {
                                List<String> tagsWithoutAuthTags = operation.getTags().stream()
                                        .filter(tag -> !isAuthTag(tag))
                                        .toList();
                                operation.setTags(tagsWithoutAuthTags);
                            }
                        })
        );
    }

    private boolean isAuthTag(Tag tag) {
        return tag.getName().equals("AUTHENTICATED");
    }

    private boolean isAuthTag(String tagName) {
        return tagName.equals("AUTHENTICATED");
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
