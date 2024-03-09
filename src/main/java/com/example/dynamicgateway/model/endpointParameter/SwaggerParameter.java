package com.example.dynamicgateway.model.endpointParameter;


import io.swagger.v3.oas.models.parameters.Parameter;

public class SwaggerParameter implements EndpointParameter {
    private final Parameter nativeParameter;

    public SwaggerParameter(String paramName) {
        this(new Parameter().name(
                (paramName == null) ? "" : paramName));
    }

    public SwaggerParameter(Parameter nativeParameter) {
        this.nativeParameter = (nativeParameter == null) ?
                new Parameter() :
                nativeParameter;
    }

    @Override
    public String getName() {
        return nativeParameter.getName();
    }

    @Override
    public boolean isRequired() {
        return (nativeParameter.getRequired() != null) &&
                nativeParameter.getRequired();
    }
}
