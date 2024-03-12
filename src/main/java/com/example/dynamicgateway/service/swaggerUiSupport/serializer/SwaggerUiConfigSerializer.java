package com.example.dynamicgateway.service.swaggerUiSupport.serializer;

import com.example.dynamicgateway.model.documentedApplication.SwaggerApplication;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import lombok.SneakyThrows;

import java.text.MessageFormat;

/**
 * A {@link JsonSerializer} that serializes {@link SwaggerApplication}s to JSONs of the following format:
 * <pre>
 * {
 *     "url": "/doc/app-name",
 *     "name": "app-name"
 * }
 * </pre>
 */
public class SwaggerUiConfigSerializer extends JsonSerializer<SwaggerApplication> {
    @Override
    @SneakyThrows
    public void serialize(SwaggerApplication swaggerApplication, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("url", MessageFormat.format("/doc/{0}", swaggerApplication.getName()));
        jsonGenerator.writeStringField("name", swaggerApplication.getName());
        jsonGenerator.writeEndObject();
    }
}
