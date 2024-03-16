package com.example.dynamicgateway.service.applicationDocClient;

import com.example.dynamicgateway.service.swaggerDocParser.OpenApiParser;
import com.example.dynamicgateway.service.swaggerDocParser.SwaggerOpenApiParser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
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
class SwaggerClientBuilderTest {
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
    void setScheme_withValidScheme_setsSuccessfully() {
        String scheme = "abc://";
        SwaggerClient swaggerClient = SwaggerClient.builder(mockWebClient)
                .setScheme(scheme)
                .build();

        assertThat(swaggerClient.getScheme()).isEqualTo(scheme);
    }

    @Test
    void setScheme_withInvalidScheme_throwsRuntimeException() {
        String invalidScheme = "invalid_scheme";
        SwaggerClient.Builder builder = SwaggerClient.builder(mockWebClient);

        assertThatThrownBy(() -> builder.setScheme(invalidScheme))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void setDocPath_withValidPath_setsSuccessfully() {
        String docPath = "/docs";
        SwaggerClient swaggerClient = SwaggerClient.builder(mockWebClient)
                .setDocPath(docPath)
                .build();

        assertThat(swaggerClient.getDocPath()).isEqualTo(docPath);
    }

    @Test
    void setDocPath_withInvalidPath_throwsRuntimeException() {
        String invalidPath = "invalid-path/";
        SwaggerClient.Builder swaggerClientConfigurer = SwaggerClient.builder(mockWebClient);

        assertThatThrownBy(() -> swaggerClientConfigurer.setScheme(invalidPath)).isInstanceOf(RuntimeException.class);
    }

    @Test
    void setParser_withNonNullParser_setsSuccessfully() {
        OpenApiParser<SwaggerParseResult> parserMock = mock(SwaggerOpenApiParser.class);

        SwaggerClient swaggerClient = SwaggerClient.builder(mockWebClient)
                .setParser(parserMock)
                .build();

        assertThat(swaggerClient.getParser()).isEqualTo(parserMock);
    }

    @Test
    @SuppressWarnings("DataFlowIssue")
    void setParser_withNullParser_throwsRuntimeException() {
        SwaggerClient.Builder builder = SwaggerClient.builder(mockWebClient);

        assertThatThrownBy(() -> builder.setParser(null)).isInstanceOf(RuntimeException.class);
    }
}