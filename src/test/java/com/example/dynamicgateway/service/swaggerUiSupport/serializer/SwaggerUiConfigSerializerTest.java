package com.example.dynamicgateway.service.swaggerUiSupport.serializer;

import com.example.dynamicgateway.model.documentedApplication.SwaggerApplication;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.text.MessageFormat;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class SwaggerUiConfigSerializerTest {
    @SneakyThrows
    @Test
    void serialize_serializesSwaggerApplicationIntoExpectedJson() {
        String appName = "test-app";
        SwaggerApplication swaggerApplicationMock = mock(SwaggerApplication.class);
        given(swaggerApplicationMock.getName()).willReturn(appName);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        JsonGenerator jsonGenerator = JsonFactory.builder().build().createGenerator(outputStream);
        SwaggerUiConfigSerializer swaggerUiConfigSerializer = new SwaggerUiConfigSerializer();

        swaggerUiConfigSerializer.serialize(swaggerApplicationMock, jsonGenerator, null);

        jsonGenerator.close();
        String actualString = outputStream.toString();
        String expectedString = MessageFormat.format("""
                '{'
                    "url": "/doc/{0}",
                    "name": "{0}"
                '}'
                """, appName);

        assertThat(StringUtils.deleteWhitespace(actualString))
                .isEqualTo(StringUtils.deleteWhitespace(expectedString));
    }
}