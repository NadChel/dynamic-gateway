package com.example.dynamicgateway.model.endpointDetails;

import com.example.dynamicgateway.model.endpointRequestBody.SwaggerRequestBody;
import io.swagger.v3.oas.models.parameters.RequestBody;
import org.junit.jupiter.api.Test;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;

class SwaggerEndpointDetailsTest {
    @Test
    void builderDoesntSetNullParametersLists() {
        SwaggerEndpointDetails endpointDetails = SwaggerEndpointDetails.builder()
                .parameters(null)
                .build();
        assertThat(endpointDetails.getParameters()).isNotNull();
        assertThat(endpointDetails.getParameters()).isEmpty();
    }

    @Test
    void builderDoesntSetNullTagList() {
        SwaggerEndpointDetails endpointDetails = SwaggerEndpointDetails.builder()
                .tags(null)
                .build();
        assertThat(endpointDetails.getTags()).isNotNull();
        assertThat(endpointDetails.getTags()).isEmpty();
    }

    @Test
    void builderDoesntSetNullRequestBody() {
        SwaggerEndpointDetails endpointDetails = SwaggerEndpointDetails.builder()
                .requestBody(null)
                .build();
        assertThat(endpointDetails.getRequestBody()).isNotNull();
        assertThat(endpointDetails.getRequestBody().isRequired()).isFalse();
    }

    @Test
    void builderSetsNonNullRequestBody() {
        RequestBody requestBody = new RequestBody();
        SwaggerEndpointDetails endpointDetails = SwaggerEndpointDetails.builder()
                .requestBody(requestBody)
                .build();
        assertThat(endpointDetails.getRequestBody())
                .extracting(this::unwrap)
                .isSameAs(requestBody);
    }

    @SuppressWarnings("DataFlowIssue")
    private RequestBody unwrap(SwaggerRequestBody requestBody) {
        Field nativeRequestBodyField =
                ReflectionUtils.findField(SwaggerRequestBody.class, "nativeRequestBody");
        assumeThat(nativeRequestBodyField).isNotNull();
        nativeRequestBodyField.setAccessible(true);
        return (RequestBody) ReflectionUtils.getField(nativeRequestBodyField, requestBody);
    }
}