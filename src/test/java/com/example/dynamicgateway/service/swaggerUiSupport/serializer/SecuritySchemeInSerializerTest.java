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

public class SecuritySchemeInSerializerTest {
    @Test
    @SneakyThrows
    void testSerialize() {
        SecurityScheme.In securitySchemeIn = SecurityScheme.In.COOKIE;

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        JsonGenerator jsonGenerator = JsonFactory.builder().build().createGenerator(outputStream);

        SecuritySchemeInSerializer securitySchemeInSerializer = new SecuritySchemeInSerializer();

        securitySchemeInSerializer.serialize(securitySchemeIn, jsonGenerator, null);

        jsonGenerator.close();

        String actualString = outputStream.toString();

        String expectedString = MessageFormat.format("\"{0}\"", securitySchemeIn.toString());

        assertThat(StringUtils.deleteWhitespace(actualString))
                .isEqualTo(StringUtils.deleteWhitespace(expectedString));
    }

}
