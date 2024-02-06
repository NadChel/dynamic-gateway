package com.example.dynamicgateway.service.applicationDocClient;

import com.example.dynamicgateway.service.swaggerDocParser.SwaggerDocParser;
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
class SwaggerClientConfigurerTest {
    @Mock
    private WebClient mockWebClient;

    @Test
    void ifNoSettersAreCalled_propertiesAreSetToSomeDefaultValues() {
        SwaggerClient swaggerClient = SwaggerClientConfigurer.configure(mockWebClient).build();
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
        SwaggerClient swaggerClient = SwaggerClientConfigurer.configure(mockWebClient)
                .setScheme(scheme)
                .build();

        assertThat(swaggerClient.getScheme()).isEqualTo(scheme);
    }

    @Test
    void ifInvalidSchemeIsSet_throwsRuntimeException() {
        String invalidScheme = "invalid_scheme";
        SwaggerClientConfigurer swaggerClientConfigurer = SwaggerClientConfigurer.configure(mockWebClient);

        assertThatThrownBy(() -> swaggerClientConfigurer.setScheme(invalidScheme)).isInstanceOf(RuntimeException.class);
    }

    @Test
    void testSetDocPath() {
        String docPath = "/docs";
        SwaggerClient swaggerClient = SwaggerClientConfigurer.configure(mockWebClient)
                .setDocPath(docPath)
                .build();

        assertThat(swaggerClient.getDocPath()).isEqualTo(docPath);
    }

    @Test
    void ifInvalidPathIsSet_throwsRuntimeException() {
        String invalidPath = "invalid-path/";
        SwaggerClientConfigurer swaggerClientConfigurer = SwaggerClientConfigurer.configure(mockWebClient);

        assertThatThrownBy(() -> swaggerClientConfigurer.setScheme(invalidPath)).isInstanceOf(RuntimeException.class);
    }

    @Test
    void testSetParser() {
        SwaggerDocParser parserMock = mock(SwaggerDocParser.class);

        SwaggerClient swaggerClient = SwaggerClientConfigurer.configure(mockWebClient)
                .setParser(parserMock)
                .build();

        assertThat(swaggerClient.getParser()).isEqualTo(parserMock);
    }

    @Test
    void ifNullParserSet_throwsRuntimeException() {
        assertThatThrownBy(() ->
                SwaggerClientConfigurer.configure(mockWebClient).setParser(null)
        ).isInstanceOf(RuntimeException.class);
    }
}