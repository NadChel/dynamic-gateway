package com.example.dynamicgateway.model.endpointRequestBody;

import io.swagger.v3.oas.models.parameters.RequestBody;

public class SwaggerRequestBody implements EndpointRequestBody {
    private final RequestBody nativeRequestBody;

    public SwaggerRequestBody(RequestBody nativeRequestBody) {
        this.nativeRequestBody = (nativeRequestBody == null) ?
                new RequestBody() :
                nativeRequestBody;
    }

    public static SwaggerRequestBody empty() {
        return new SwaggerRequestBody(new RequestBody());
    }

    @Override
    public boolean isRequired() {
        return (nativeRequestBody.getRequired() != null) &&
                nativeRequestBody.getRequired();
    }
}
