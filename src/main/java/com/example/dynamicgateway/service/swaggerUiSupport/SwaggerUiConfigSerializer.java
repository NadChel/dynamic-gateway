package com.example.dynamicgateway.service.swaggerUiSupport;

import com.example.dynamicgateway.model.documentedApplication.SwaggerApplication;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import lombok.SneakyThrows;

import java.text.MessageFormat;

public class SwaggerUiConfigSerializer extends JsonSerializer<SwaggerApplication> {
    @Override
    @SneakyThrows
    public void serialize(SwaggerApplication swaggerApplication, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("url", MessageFormat.format("/{0}/doc", swaggerApplication.getName()));
        jsonGenerator.writeStringField("name", swaggerApplication.getName());
        jsonGenerator.writeEndObject();
    }
}
