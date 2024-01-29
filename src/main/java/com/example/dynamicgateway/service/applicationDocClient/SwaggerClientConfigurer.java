package com.example.dynamicgateway.service.applicationDocClient;

import com.example.dynamicgateway.model.discoverableApplication.DiscoverableApplication;
import com.example.dynamicgateway.model.discoverableApplication.EurekaDiscoverableApplication;
import com.example.dynamicgateway.model.documentedApplication.SwaggerApplication;
import com.example.dynamicgateway.service.swaggerDocParser.SwaggerDocParser;
import com.example.dynamicgateway.service.swaggerDocParser.V3SwaggerDocParser;
import com.example.dynamicgateway.util.UriValidator;
import io.swagger.v3.parser.OpenAPIV3Parser;
import lombok.Getter;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Class for configuring and creating {@link SwaggerClient} instances
 */
public class SwaggerClientConfigurer {
    @Getter
    private String scheme = EurekaDiscoverableApplication.LB_SCHEME;
    @Getter
    private String docPath = SwaggerApplication.V3_DOC_PATH;
    @Getter
    private SwaggerDocParser parser = new V3SwaggerDocParser();
    private final WebClient resolvingWebClient;

    private SwaggerClientConfigurer(WebClient resolvingWebClient) {
        this.resolvingWebClient = resolvingWebClient;
    }

    /**
     * Factory method returning a {@code SwaggerClientConfigurer} instance
     *
     * @param resolvingWebClient {@code WebClient} that is capable of resolving {@link DiscoverableApplication}
     *                           names into specific servers
     * @return {@code SwaggerClientConfigurer} with default field values
     */
    public static SwaggerClientConfigurer configure(WebClient resolvingWebClient) {
        return new SwaggerClientConfigurer(resolvingWebClient);
    }

    /**
     * Sets this scheme unless the passed string does not represent a valid scheme
     * (one or more lower-case Latin letters followed by a colon and two forward slashes).
     * If the method is not invoked, the scheme defaults to {@link EurekaDiscoverableApplication#LB_SCHEME}
     *
     * @return this {@code SwaggerClientConfigurer}
     * @throws IllegalArgumentException if the passed scheme string is invalid
     */
    public SwaggerClientConfigurer setScheme(String scheme) {
        UriValidator.requireValidScheme(scheme);
        this.scheme = scheme;
        return this;
    }

    /**
     * Sets this documentation path.
     * If the method is not invoked, the path defaults to {@link SwaggerApplication#V3_DOC_PATH}
     *
     * @return this {@code SwaggerClientConfigurer}
     * @throws IllegalArgumentException if the passed path string is invalid
     */
    public SwaggerClientConfigurer setDocPath(String docPath) {
        UriValidator.requireValidPath(docPath);
        this.docPath = docPath;
        return this;
    }

    /**
     * Sets this {@link SwaggerDocParser}.
     * If the method is not invoked, the parser defaults to an instance of {@link OpenAPIV3Parser}
     *
     * @return this {@code SwaggerClientConfigurer}
     */
    public SwaggerClientConfigurer setParser(SwaggerDocParser parser) {
        this.parser = parser;
        return this;
    }

    /**
     * Returns a {@code SwaggerClient} instance constructed from this {@code SwaggerClientConfigurer}
     */
    public SwaggerClient build() {
        return new SwaggerClient(this);
    }

    public WebClient getWebClient() {
        return resolvingWebClient;
    }
}
