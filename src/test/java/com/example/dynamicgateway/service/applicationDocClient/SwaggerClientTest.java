package com.example.dynamicgateway.service.applicationDocClient;

import com.example.dynamicgateway.model.discoverableApplication.DiscoverableApplication;
import com.example.dynamicgateway.service.swaggerDocParser.OpenApiParser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.BDDMockito.given;

class SwaggerClientTest {
    @SneakyThrows
    @Test
    void testFindApplicationDoc() {
        String scheme = "test://";
        String appName = "test-application";
        String docPath = "/doc";

        String serializedParseResult = "{ let's imagine: it's a serialized parse result }";
        SwaggerParseResult parseResult = mock(SwaggerParseResult.class);
        OpenApiParser parserMock = mock(OpenApiParser.class);
        given(parserMock.parse(serializedParseResult)).willReturn(parseResult);

        WebClient webClientMock = mock(WebClient.class, RETURNS_DEEP_STUBS);
        given(webClientMock
                .get()
                .uri(scheme + appName + docPath)
                .retrieve()
                .bodyToMono(String.class)
        ).willReturn(Mono.just(serializedParseResult));

        SwaggerClient.Builder swaggerClientConfigurerMock = mock(SwaggerClient.Builder.class);
        given(swaggerClientConfigurerMock.getWebClient()).willReturn(webClientMock);
        given(swaggerClientConfigurerMock.getParser()).willReturn(parserMock);
        given(swaggerClientConfigurerMock.getScheme()).willReturn(scheme);
        given(swaggerClientConfigurerMock.getDocPath()).willReturn(docPath);

        DiscoverableApplication<?> discoverableApplicationMock = mock(DiscoverableApplication.class);
        given(discoverableApplicationMock.getName()).willReturn(appName);

        SwaggerClient swaggerClient = new SwaggerClient(swaggerClientConfigurerMock);
        Mono<SwaggerParseResult> applicationDoc = swaggerClient.findApplicationDoc(discoverableApplicationMock);

        StepVerifier.create(applicationDoc)
                .expectNext(parseResult)
                .expectComplete()
                .verify();
    }
}