package com.example.dynamicgateway.model.documentedApplication;

import com.example.dynamicgateway.model.discoverableApplication.DiscoverableApplication;
import com.example.dynamicgateway.model.documentedEndpoint.SwaggerEndpoint;
import com.example.dynamicgateway.model.endpointDetails.SwaggerEndpointDetails;
import com.example.dynamicgateway.testModel.SwaggerEndpointStub;
import com.example.dynamicgateway.testUtil.SwaggerParseResultGenerator;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;

import java.text.MessageFormat;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

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
        given(discoverableApp.getName()).willReturn(appName);

        SwaggerParseResult parseResult = SwaggerParseResultGenerator.createForEndpoints(info, endpoints);

        SwaggerApplication swaggerApplication = new SwaggerApplication(discoverableApp, parseResult);

        assertThat(swaggerApplication.getDescription()).isEqualTo(description);
        assertThat(swaggerApplication.getEndpoints()).asList().containsExactlyInAnyOrderElementsOf(endpoints);
    }

    @Test
    void equalsHashCodeContract() {
        DiscoverableApplication<?> discoverableAppMock = mock(DiscoverableApplication.class);
        DiscoverableApplication<?> anotherDiscoverableAppMock = mock(DiscoverableApplication.class);
        given(discoverableAppMock.getName()).willReturn("app");
        given(anotherDiscoverableAppMock.getName()).willReturn("another-app");

        SwaggerApplication swaggerAppMock = new SwaggerApplication(discoverableAppMock,
                SwaggerParseResultGenerator.empty());
        SwaggerApplication anotherSwaggerAppMock = new SwaggerApplication(anotherDiscoverableAppMock,
                SwaggerParseResultGenerator.empty());

        EqualsVerifier.forClass(SwaggerApplication.class)
                .withPrefabValues(DiscoverableApplication.class,
                        discoverableAppMock, anotherDiscoverableAppMock)
                .withPrefabValues(SwaggerEndpoint.class,
                        new SwaggerEndpoint(swaggerAppMock, SwaggerEndpointDetails.builder().build()),
                        new SwaggerEndpoint(anotherSwaggerAppMock, SwaggerEndpointDetails.builder().build()))
                .withPrefabValues(SwaggerParseResult.class,
                        SwaggerParseResultGenerator.empty(),
                        SwaggerParseResultGenerator.createForEndpoints(new Info().description("some-description")))
                .withNonnullFields("discoverableApplication")
                .suppress(Warning.ALL_FIELDS_SHOULD_BE_USED)
                .verify();
    }

    @Test
    void toString_returnsExpectedString() {
        String appName = "some-app";
        DiscoverableApplication<?> discoverableAppMock = mock(DiscoverableApplication.class);
        given(discoverableAppMock.getName()).willReturn(appName);
        SwaggerApplication swaggerApplication = new SwaggerApplication(discoverableAppMock,
                SwaggerParseResultGenerator.empty());
        assertThat(swaggerApplication.toString()).isEqualTo(MessageFormat.format("{0} {1}",
                SwaggerApplication.class.getSimpleName(), appName));
    }
}