package com.example.dynamicgateway.client;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

@ExtendWith(MockitoExtension.class)
class SwaggerClientConfigurerTest {
    @Mock
    WebClient mockWebClient;
    @Test
    void ifNoSettersAreCalled_propertiesAreSetToSomeDefaultValues() {
        SwaggerClient swaggerClient = SwaggerClientConfigurer.configure(mockWebClient).build();
        List<Object> fieldValues = Arrays.stream(swaggerClient.getClass().getDeclaredFields())
                .peek(field -> field.setAccessible(true))
                .map(field -> ReflectionUtils.getField(field, swaggerClient))
                .toList();
        assertSoftly(softAssertions -> softAssertions.assertThatIterable(fieldValues).allMatch(Objects::nonNull));
    }

    @Test
    void testSetScheme() {
        String testScheme = "abc://";
        SwaggerClient swaggerClient = SwaggerClientConfigurer.configure(mockWebClient)
                .setScheme(testScheme)
                .build();
        assertThat(swaggerClient.getScheme()).isEqualTo(testScheme);
    }

    @Test
    void ifInvalidSchemeIsSet_throwsRuntimeException() {
        String invalidScheme = "invalid_scheme";
        SwaggerClientConfigurer swaggerClientConfigurer = SwaggerClientConfigurer.configure(mockWebClient);
        assertThatThrownBy(() -> swaggerClientConfigurer.setScheme(invalidScheme)).isInstanceOf(RuntimeException.class);
    }

    @Test
    void testSetDocPath() {
        String testDocPath = "/docs";
        SwaggerClient swaggerClient = SwaggerClientConfigurer.configure(mockWebClient)
                .setDocPath(testDocPath)
                .build();
        assertThat(swaggerClient.getDocPath()).isEqualTo(testDocPath);
    }
}