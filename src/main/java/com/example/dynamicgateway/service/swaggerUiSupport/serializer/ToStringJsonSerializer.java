package com.example.dynamicgateway.service.swaggerUiSupport.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * A {@link JsonSerializer} that delegates serialization to the object's {@link Object#toString()}
 * method
 *
 * @param <T> type of the serialized object
 */
public class ToStringJsonSerializer<T> extends JsonSerializer<T> {
    @Override
    public void serialize(T object, JsonGenerator jsonGenerator,
                          SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeString(object.toString());
    }
}
