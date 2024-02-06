package com.example.dynamicgateway.service.applicationDocClient;

import com.example.dynamicgateway.model.discoverableApplication.DiscoverableApplication;
import com.example.dynamicgateway.service.swaggerDocParser.SwaggerDocParser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SwaggerClientTest {
    @SneakyThrows
    @Test
    void testFindApplicationDoc() {
        String scheme = "test://";
        String appName = "test-application";
        String docPath = "/doc";

        String serializedParseResult = "{ let's imagine: it's a serialized parse result }";
        SwaggerParseResult parseResult = mock(SwaggerParseResult.class);
        SwaggerDocParser parserMock = mock(SwaggerDocParser.class);
        when(parserMock.parse(serializedParseResult)).thenReturn(parseResult);

        WebClient webClientMock = mock(WebClient.class, RETURNS_DEEP_STUBS);
        when(webClientMock
                .get()
                .uri(scheme + appName + docPath)
                .retrieve()
                .bodyToMono(String.class)
        ).thenReturn(Mono.just(serializedParseResult));

        SwaggerClientConfigurer swaggerClientConfigurerMock = mock(SwaggerClientConfigurer.class);
        when(swaggerClientConfigurerMock.getWebClient()).thenReturn(webClientMock);
        when(swaggerClientConfigurerMock.getParser()).thenReturn(parserMock);
        when(swaggerClientConfigurerMock.getScheme()).thenReturn(scheme);
        when(swaggerClientConfigurerMock.getDocPath()).thenReturn(docPath);

        DiscoverableApplication<?> discoverableApplicationMock = mock(DiscoverableApplication.class);
        when(discoverableApplicationMock.getName()).thenReturn(appName);

        SwaggerClient swaggerClient = new SwaggerClient(swaggerClientConfigurerMock);
        Mono<SwaggerParseResult> applicationDoc = swaggerClient.findApplicationDoc(discoverableApplicationMock);

        StepVerifier.create(applicationDoc)
                .expectNext(parseResult)
                .expectComplete()
                .verify();
    }
}