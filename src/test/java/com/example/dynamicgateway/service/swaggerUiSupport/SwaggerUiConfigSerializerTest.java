package com.example.dynamicgateway.service.swaggerUiSupport;

import com.example.dynamicgateway.model.documentedApplication.SwaggerApplication;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.text.MessageFormat;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SwaggerUiConfigSerializerTest {
    @SneakyThrows
    @Test
    void serialize() {
        String appName = "test-app";

        SwaggerApplication swaggerApplicationMock = mock(SwaggerApplication.class);
        when(swaggerApplicationMock.getName()).thenReturn(appName);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        JsonGenerator jsonGenerator = JsonFactory.builder().build().createGenerator(outputStream);

        SwaggerUiConfigSerializer swaggerUiConfigSerializer = new SwaggerUiConfigSerializer();

        swaggerUiConfigSerializer.serialize(swaggerApplicationMock, jsonGenerator, null);

        jsonGenerator.close();

        String resultJson = outputStream.toString();

        String expectedString = MessageFormat.format("""
                '{'
                    "url": "/doc/{0}",
                    "name": "{0}"
                '}'
                """, appName);

        assertThat(StringUtils.deleteWhitespace(resultJson))
                .isEqualTo(StringUtils.deleteWhitespace(expectedString));
    }
}