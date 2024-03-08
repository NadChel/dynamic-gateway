package com.example.dynamicgateway.model.endpointRequestBody;

import io.swagger.v3.oas.models.parameters.RequestBody;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SwaggerRequestBodyTest {
    @Test
    void ifNullRequestBodyPassed_isRequiredReturnsFalse() {
        SwaggerRequestBody requestBody = new SwaggerRequestBody(null);
        assertThat(requestBody.isRequired()).isFalse();
    }

    @Test
    void ifDefaultRequestBodyPassed_isRequiredReturnsFalse() {
        SwaggerRequestBody requestBody = new SwaggerRequestBody(new RequestBody());
        assertThat(requestBody.isRequired()).isFalse();
    }

    @Test
    void ifNonRequiredRequestBodyPassed_isRequiredReturnsFalse() {
        SwaggerRequestBody requestBody = new SwaggerRequestBody(new RequestBody().required(false));
        assertThat(requestBody.isRequired()).isFalse();
    }

    @Test
    void ifRequiredRequestBodyPassed_isRequiredReturnsTrue() {
        SwaggerRequestBody requestBody = new SwaggerRequestBody(new RequestBody().required(true));
        assertThat(requestBody.isRequired()).isTrue();
    }
}