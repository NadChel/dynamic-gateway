package com.example.dynamicgateway.model.endpointParameter;


import io.swagger.v3.oas.models.parameters.Parameter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SwaggerParameter implements EndpointParameter {
    private final Parameter parameter;

    @Override
    public String getName() {
        return parameter.getName();
    }

    @Override
    public boolean isRequired() {
        return parameter.getRequired();
    }
}
