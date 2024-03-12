package com.example.dynamicgateway.service.swaggerUiSupport.serializer;

import io.swagger.v3.oas.models.security.SecurityScheme;

public class SecuritySchemeInJsonSerializer extends ToStringJsonSerializer<SecurityScheme.In> {
    @Override
    public Class<SecurityScheme.In> handledType() {
        return SecurityScheme.In.class;
    }
}
