package com.example.dynamicgateway.service.swaggerUiSupport.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.swagger.v3.oas.models.security.SecurityScheme;

import java.io.IOException;

public class SecuritySchemeTypeSerializer extends JsonSerializer<SecurityScheme.Type> {
    @Override
    public void serialize(SecurityScheme.Type securitySchemeType, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeString(securitySchemeType.toString());
    }

    @Override
    public Class<SecurityScheme.Type> handledType() {
        return SecurityScheme.Type.class;
    }
}
