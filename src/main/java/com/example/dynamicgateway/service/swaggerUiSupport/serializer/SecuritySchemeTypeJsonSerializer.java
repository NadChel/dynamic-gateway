package com.example.dynamicgateway.service.swaggerUiSupport.serializer;

import io.swagger.v3.oas.models.security.SecurityScheme;

public class SecuritySchemeTypeJsonSerializer extends ToStringJsonSerializer<SecurityScheme.Type> {
    @Override
    public Class<SecurityScheme.Type> handledType() {
        return SecurityScheme.Type.class;
    }
}
