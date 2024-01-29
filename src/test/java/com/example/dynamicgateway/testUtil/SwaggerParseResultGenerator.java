package com.example.dynamicgateway.testUtil;

import com.example.dynamicgateway.model.documentedEndpoint.SwaggerEndpoint;
import com.example.dynamicgateway.model.endpointDetails.SwaggerEndpointDetails;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.parser.core.models.SwaggerParseResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SwaggerParseResultGenerator {
    public SwaggerParseResult createForEndpoints(List<SwaggerEndpoint> endpoints) {
        SwaggerParseResult parseResult = new SwaggerParseResult();
        OpenAPI openApi = createOpenApi(endpoints);
        parseResult.setOpenAPI(openApi);
        return parseResult;
    }

    private OpenAPI createOpenApi(List<SwaggerEndpoint> endpoints) {
        OpenAPI openApi = new OpenAPI();
        openApi.setInfo(createInfo());
        openApi.setPaths(createPaths(endpoints));
        return openApi;
    }

    private Info createInfo() {
        Info info = new Info();
        info.setDescription("Test description");
        return info;
    }

    private Paths createPaths(List<SwaggerEndpoint> endpoints) {
        Paths paths = new Paths();
        Map<String, List<SwaggerEndpointDetails>> pathToDetails = createPathToDetailsMap(endpoints);
        for (Map.Entry<String, List<SwaggerEndpointDetails>> pathEntry : pathToDetails.entrySet()) {
            String path = pathEntry.getKey();
            PathItem pathItem = createPathItem(pathEntry.getValue());
            paths.addPathItem(path, pathItem);
        }
        return paths;
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

    private PathItem createPathItem(List<SwaggerEndpointDetails> detailsForPath) {
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
}
