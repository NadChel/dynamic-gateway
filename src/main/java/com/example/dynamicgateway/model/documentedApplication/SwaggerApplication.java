package com.example.dynamicgateway.model.documentedApplication;

import com.example.dynamicgateway.model.discoverableApplication.DiscoverableApplication;
import com.example.dynamicgateway.model.documentedEndpoint.SwaggerEndpoint;
import com.example.dynamicgateway.model.endpointDetails.SwaggerEndpointDetails;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import lombok.Getter;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * {@link DocumentedApplication} that exposes its API by means of Swagger (Open API)
 */
public class SwaggerApplication implements DocumentedApplication<SwaggerParseResult> {
    public static final String V3_DOC_PATH = "/v3/api-docs";
    private final DiscoverableApplication<?> application;
    @Getter
    private final String description;
    private final List<SwaggerEndpoint> endpoints;
    private final SwaggerParseResult doc;

    public SwaggerApplication(DiscoverableApplication<?> application, SwaggerParseResult parseResult) {
        this.application = application;
        this.doc = parseResult;
        this.description = extractDescription(parseResult);
        this.endpoints = extractEndpoints(parseResult);
    }

    private String extractDescription(SwaggerParseResult parseResult) {
        return parseResult.getOpenAPI().getInfo().getDescription();
    }

    private List<SwaggerEndpoint> extractEndpoints(SwaggerParseResult parseResult) {
        return parseResult.getOpenAPI()
                .getPaths()
                .entrySet()
                .stream()
                .flatMap(this::getEndpointStream)
                .toList();
    }

    private Stream<SwaggerEndpoint> getEndpointStream(Map.Entry<String, PathItem> pathPathItemEntry) {
        return pathPathItemEntry.getValue()
                .readOperationsMap()
                .entrySet()
                .stream()
                .map(methodOperationEntry -> {
                    SwaggerEndpointDetails endpointDetails = buildEndpointDetails(pathPathItemEntry, methodOperationEntry);
                    return new SwaggerEndpoint(this, endpointDetails);
                })
                .toList()
                .stream();
    }

    private SwaggerEndpointDetails buildEndpointDetails(Map.Entry<String, PathItem> pathPathItemEntry,
                                                        Map.Entry<PathItem.HttpMethod, Operation> methodOperationEntry) {
        return SwaggerEndpointDetails.builder()
                .setMethod(methodOperationEntry.getKey())
                .setPath(pathPathItemEntry.getKey())
                .setParameters(methodOperationEntry.getValue().getParameters())
                .setRequestBody(methodOperationEntry.getValue().getRequestBody())
                .setTags(methodOperationEntry.getValue().getTags())
                .build();
    }

    @Override
    public DiscoverableApplication<?> getDiscoverableApp() {
        return application;
    }

    @Override
    public List<SwaggerEndpoint> getEndpoints() {
        return List.copyOf(endpoints);
    }

    @Override
    public SwaggerParseResult getNativeDoc() {
        return doc;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SwaggerApplication that)) return false;

        return application.getName().equals(that.application.getName());
    }

    @Override
    public int hashCode() {
        return application.getName().hashCode();
    }

    @Override
    public String toString() {
        return MessageFormat.format("SwaggerApplication {0}", application.getName());
    }
}
