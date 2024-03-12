package com.example.dynamicgateway.service.swaggerUiSupport.serializer;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.text.MessageFormat;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class SecuritySchemeTypeSerializerTest {
    @Test
    @SneakyThrows
    void testSerialize() {
        SecurityScheme.Type securitySchemeType = SecurityScheme.Type.APIKEY;

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        JsonGenerator jsonGenerator = JsonFactory.builder().build().createGenerator(outputStream);

        SecuritySchemeTypeJsonSerializer securitySchemeTypeSerializer = new SecuritySchemeTypeJsonSerializer();

        securitySchemeTypeSerializer.serialize(securitySchemeType, jsonGenerator, null);

        jsonGenerator.close();

        String actualString = outputStream.toString();

        String expectedString = MessageFormat.format("\"{0}\"", securitySchemeType.toString());

        assertThat(StringUtils.deleteWhitespace(actualString))
                .isEqualTo(StringUtils.deleteWhitespace(expectedString));
    }
}
