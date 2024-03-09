package com.example.dynamicgateway.service.applicationDocClient;

import com.example.dynamicgateway.service.swaggerDocParser.OpenApiParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class BuilderTest {
    @Mock
    private WebClient mockWebClient;

    @Test
    void ifNoSettersAreCalled_propertiesAreSetToSomeDefaultValues() {
        SwaggerClient swaggerClient = SwaggerClient.builder(mockWebClient).build();
        List<Object> fieldValues = Arrays.stream(swaggerClient.getClass().getDeclaredFields())
                .peek(field -> field.setAccessible(true))
                .map(field -> ReflectionUtils.getField(field, swaggerClient))
                .toList();

        assertSoftly(soft -> {
            for (Object fieldValue : fieldValues) {
                soft.assertThat(fieldValue).isNotNull();
            }
        });
    }

    @Test
    void testSetScheme() {
        String scheme = "abc://";
        SwaggerClient swaggerClient = SwaggerClient.builder(mockWebClient)
                .setScheme(scheme)
                .build();

        assertThat(swaggerClient.getScheme()).isEqualTo(scheme);
    }

    @Test
    void ifInvalidSchemeIsSet_throwsRuntimeException() {
        String invalidScheme = "invalid_scheme";
        SwaggerClient.Builder swaggerClientConfigurer = SwaggerClient.builder(mockWebClient);

        assertThatThrownBy(() -> swaggerClientConfigurer.setScheme(invalidScheme)).isInstanceOf(RuntimeException.class);
    }

    @Test
    void testSetDocPath() {
        String docPath = "/docs";
        SwaggerClient swaggerClient = SwaggerClient.builder(mockWebClient)
                .setDocPath(docPath)
                .build();

        assertThat(swaggerClient.getDocPath()).isEqualTo(docPath);
    }

    @Test
    void ifInvalidPathIsSet_throwsRuntimeException() {
        String invalidPath = "invalid-path/";
        SwaggerClient.Builder swaggerClientConfigurer = SwaggerClient.builder(mockWebClient);

        assertThatThrownBy(() -> swaggerClientConfigurer.setScheme(invalidPath)).isInstanceOf(RuntimeException.class);
    }

    @Test
    void testSetParser() {
        OpenApiParser parserMock = mock(OpenApiParser.class);

        SwaggerClient swaggerClient = SwaggerClient.builder(mockWebClient)
                .setParser(parserMock)
                .build();

        assertThat(swaggerClient.getParser()).isEqualTo(parserMock);
    }

    @Test
    void ifNullParserSet_throwsRuntimeException() {
        assertThatThrownBy(() ->
                SwaggerClient.builder(mockWebClient).setParser(null)
        ).isInstanceOf(RuntimeException.class);
    }
}