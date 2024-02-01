package com.example.dynamicgateway.service.applicationDocClient;

import com.example.dynamicgateway.model.discoverableApplication.DiscoverableApplication;
import com.example.dynamicgateway.service.swaggerDocParser.SwaggerDocParser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import lombok.Getter;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * An {@link ApplicationDocClient} implementation for finding Swagger (Open API) documentation exposed by {@link DiscoverableApplication}s
 */
@Getter
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
    public Mono<SwaggerParseResult> findApplicationDoc(DiscoverableApplication application) {
        return webClient
                .get()
                .uri(scheme + application.getName() + docPath)
                .retrieve()
                .bodyToMono(String.class)
                .map(parser::parse);
    }
}
