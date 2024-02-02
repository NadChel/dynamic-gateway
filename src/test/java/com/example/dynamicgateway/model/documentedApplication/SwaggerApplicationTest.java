package com.example.dynamicgateway.model.documentedApplication;

import com.example.dynamicgateway.model.discoverableApplication.DiscoverableApplication;
import com.example.dynamicgateway.model.documentedEndpoint.SwaggerEndpoint;
import com.example.dynamicgateway.testModel.SwaggerEndpointStub;
import com.example.dynamicgateway.testUtil.SwaggerParseResultGenerator;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SwaggerApplicationTest {
    @Test
    void createdApplication_containsExpectedData() {
        String description = "This is some kick-ass app";
        Info info = new Info().description(description);

        String appName = "cool-app";

        List<SwaggerEndpoint> endpoints = List.of(
                SwaggerEndpointStub.builder()
                        .declaringAppName(appName)
                        .method(HttpMethod.GET)
                        .path("/auth/test-path")
                        .build(),
                SwaggerEndpointStub.builder()
                        .declaringAppName(appName)
                        .method(HttpMethod.TRACE)
                        .path("/auth/test-path-two")
                        .build()
        );

        DiscoverableApplication<?> discoverableApp = mock(DiscoverableApplication.class);
        when(discoverableApp.getName()).thenReturn(appName);

        SwaggerParseResult parseResult = SwaggerParseResultGenerator.createForEndpoints(info, endpoints);

        SwaggerApplication swaggerApplication = new SwaggerApplication(discoverableApp, parseResult);

        assertThat(swaggerApplication.getDescription()).isEqualTo(description);
        assertThat(swaggerApplication.getEndpoints()).asList().containsExactlyInAnyOrderElementsOf(endpoints);
    }
}