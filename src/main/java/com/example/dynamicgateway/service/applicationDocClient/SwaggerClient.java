package com.example.dynamicgateway.service.applicationDocClient;

import com.example.dynamicgateway.model.discoverableApplication.DiscoverableApplication;
import com.example.dynamicgateway.model.discoverableApplication.EurekaDiscoverableApplication;
import com.example.dynamicgateway.model.documentedApplication.SwaggerApplication;
import com.example.dynamicgateway.service.swaggerDocParser.OpenApiParser;
import com.example.dynamicgateway.service.swaggerDocParser.SwaggerOpenApiParser;
import com.example.dynamicgateway.util.UriValidator;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.text.MessageFormat;
import java.util.Objects;
import java.util.function.Function;

/**
 * An {@link ApplicationDocClient} implementation for finding Swagger (Open API) documentation
 * potentially exposed by {@link DiscoverableApplication}s
 */
@Getter
@Slf4j
public class SwaggerClient implements ApplicationDocClient<SwaggerParseResult> {
    private final String scheme;
    private final String docPath;
    private final WebClient webClient;
    private final OpenApiParser parser;

    SwaggerClient(Builder builder) {
        this.scheme = builder.getScheme();
        this.docPath = builder.getDocPath();
        this.webClient = builder.getWebClient();
        this.parser = builder.getParser();
    }

    /**
     * Returns a builder for configuring a {@code SwaggerClient} object
     *
     * @param resolvingWebClient {@code WebClient} that is capable of resolving {@link DiscoverableApplication}
     *                           names into specific servers
     * @return {@code Builder} with default field values
     */
    public static Builder builder(WebClient resolvingWebClient) {
        return new Builder(resolvingWebClient);
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
                    {0} exposes its API, and this SwaggerClient is configured \
                    properly (e.g. docPath is correct) \
                    """, application.getName()));
            return Mono.empty();
        };
    }

    public static class Builder {
        @Getter
        private String scheme = EurekaDiscoverableApplication.LB_SCHEME;
        @Getter
        private String docPath = SwaggerApplication.V3_DOC_PATH;
        @Getter
        private OpenApiParser parser = new SwaggerOpenApiParser();
        private final WebClient resolvingWebClient;

        private Builder(@NonNull WebClient resolvingWebClient) {
            this.resolvingWebClient = resolvingWebClient;
        }

        /**
         * Sets this scheme unless the passed string does not represent a valid scheme
         * according to the {@link UriValidator}.
         * If the method is not invoked, the scheme defaults to {@link EurekaDiscoverableApplication#LB_SCHEME}
         *
         * @return this {@code Builder}
         * @throws IllegalArgumentException if the passed scheme string is invalid
         * @see UriValidator
         */
        public Builder setScheme(@NonNull String scheme) {
            UriValidator.requireValidScheme(scheme);
            this.scheme = scheme;
            return this;
        }

        /**
         * Sets this documentation path unless the passed string does not represent a valid path
         * according to the {@link UriValidator}.
         * If the method is not invoked, the path defaults to {@link SwaggerApplication#V3_DOC_PATH}
         *
         * @return this {@code Builder}
         * @throws IllegalArgumentException if the passed path string is invalid
         * @see UriValidator
         */
        public Builder setDocPath(@NonNull String docPath) {
            UriValidator.requireValidPath(docPath);
            this.docPath = docPath;
            return this;
        }

        /**
         * Sets this {@link OpenApiParser}.
         * If the method is not invoked, the parser defaults to an instance of {@link SwaggerOpenApiParser}
         *
         * @return this {@code Builder}
         * @throws NullPointerException if the provided parser is {@code null}
         */
        public Builder setParser(@NonNull OpenApiParser parser) {
            Objects.requireNonNull(parser, "Parser cannot be null");
            this.parser = parser;
            return this;
        }

        public WebClient getWebClient() {
            return resolvingWebClient;
        }

        /**
         * Returns a {@code SwaggerClient} instance constructed from this {@code Builder}
         */
        public SwaggerClient build() {
            return new SwaggerClient(this);
        }
    }
}
