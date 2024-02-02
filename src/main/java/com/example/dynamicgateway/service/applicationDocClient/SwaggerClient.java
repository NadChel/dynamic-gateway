package com.example.dynamicgateway.service.applicationDocClient;

import com.example.dynamicgateway.model.discoverableApplication.DiscoverableApplication;
import com.example.dynamicgateway.service.swaggerDocParser.SwaggerDocParser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.text.MessageFormat;
import java.util.function.Function;

/**
 * An {@link ApplicationDocClient} implementation for finding Swagger (Open API) documentation exposed by {@link DiscoverableApplication}s
 */
@Getter
@Slf4j
public class SwaggerClient implements ApplicationDocClient<SwaggerParseResult> {
    private final String scheme;
    private final String docPath;
    private final WebClient webClient;
    private final SwaggerDocParser parser;

    SwaggerClient(SwaggerClientConfigurer configurer) {
        this.scheme = configurer.getScheme();
        this.docPath = configurer.getDocPath();
        this.webClient = configurer.getWebClient();
        this.parser = configurer.getParser();
    }

    @Override
    public Mono<SwaggerParseResult> findApplicationDoc(DiscoverableApplication<?> application) {
        return webClient
                .get()
                .uri(scheme + application.getName() + docPath)
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(WebClientResponseException.NotFound.class,
                        handle404NotFound(application))
                .map(parser::parse);
    }

    private Function<WebClientResponseException.NotFound, Mono<? extends String>> handle404NotFound(DiscoverableApplication<?> application) {
        return ex -> {
            log.warn(MessageFormat.format("""
                    Could not find doc for {0}. If it''s not expected, make sure \
                    {0} exposes its API and this SwaggerClient is configured \
                    properly (e.g. docPath is correct) \
                    """, application.getName()));
            return Mono.empty();
        };
    }
}
