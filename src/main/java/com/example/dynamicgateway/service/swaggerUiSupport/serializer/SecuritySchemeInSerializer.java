package com.example.dynamicgateway.service.swaggerUiSupport.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.swagger.v3.oas.models.security.SecurityScheme;

import java.io.IOException;

public class SecuritySchemeInSerializer extends JsonSerializer<SecurityScheme.In > {
    @Override
    public void serialize(SecurityScheme.In  securitySchemeIn, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeString(securitySchemeIn.toString());
    }

    @Override
    public Class<SecurityScheme.In > handledType() {
        return SecurityScheme.In.class;
    }
}
