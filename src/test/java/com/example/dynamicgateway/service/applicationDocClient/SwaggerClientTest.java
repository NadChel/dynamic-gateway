package com.example.dynamicgateway.service.applicationDocClient;

import com.example.dynamicgateway.model.discoverableApplication.DiscoverableApplication;
import com.example.dynamicgateway.service.swaggerDocParser.OpenApiParser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.BDDMockito.given;

class SwaggerClientTest {
    @SneakyThrows
    @Test
    void findApplicationDoc_ifDocAvailable_returnsParsedDoc() {
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

        DiscoverableApplication<?> discoverableApplicationMock = mock(DiscoverableApplication.class);
        given(discoverableApplicationMock.getName()).willReturn(appName);

        SwaggerClient swaggerClient = SwaggerClient.builder(webClientMock)
                .setScheme(scheme)
                .setDocPath(docPath)
                .setParser(parserMock)
                .build();
        Mono<SwaggerParseResult> applicationDoc = swaggerClient.findApplicationDoc(discoverableApplicationMock);

        StepVerifier.create(applicationDoc)
                .expectNext(parseResult)
                .expectComplete()
                .verify();
    }

    @Test
    void findApplicationDoc_ifDocUnavailable_returnsEmptyMono() {
        String scheme = "test://";
        String appName = "test-application";
        String docPath = "/doc";

        WebClientResponseException notFoundException = WebClientResponseException.create(
                HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.getReasonPhrase(), HttpHeaders.EMPTY,
                new byte[0], null, null
        );

        WebClient webClientMock = mock(WebClient.class, RETURNS_DEEP_STUBS);
        given(webClientMock
                .get()
                .uri(scheme + appName + docPath)
                .retrieve()
                .bodyToMono(String.class)
        ).willReturn(Mono.error(notFoundException));

        SwaggerClient swaggerClient = SwaggerClient.builder(webClientMock)
                .setScheme(scheme)
                .setDocPath(docPath)
                .build();

        DiscoverableApplication<?> discoverableApplicationMock = mock(DiscoverableApplication.class);
        given(discoverableApplicationMock.getName()).willReturn(appName);
        Mono<SwaggerParseResult> applicationDoc = swaggerClient.findApplicationDoc(discoverableApplicationMock);

        StepVerifier.create(applicationDoc)
                .verifyComplete();
    }
}