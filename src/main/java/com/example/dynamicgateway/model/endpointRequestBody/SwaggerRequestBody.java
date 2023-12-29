package com.example.dynamicgateway.model.endpointRequestBody;

import io.swagger.v3.oas.models.parameters.RequestBody;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SwaggerRequestBody implements EndpointRequestBody {
    private final RequestBody requestBody;

    public static SwaggerRequestBody empty() {
        return new SwaggerRequestBody(new RequestBody());
    }

    @Override
    public boolean isRequired() {
        return (requestBody.getRequired() != null) &&
                requestBody.getRequired();
    }
}
