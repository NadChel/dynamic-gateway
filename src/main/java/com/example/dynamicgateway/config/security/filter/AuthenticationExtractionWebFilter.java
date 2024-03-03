package com.example.dynamicgateway.config.security.filter;

import com.example.dynamicgateway.service.authenticationExtractor.AuthenticationExtractor;
import com.example.dynamicgateway.util.ResponseWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * A {@link WebFilter} that tries to build an {@link Authentication} object from {@code ServerWebExchange}
 * and pass it to the {@link ReactiveSecurityContextHolder}.
 * <p>
 * This class is oblivious to the location of authentication claims and relies on an {@link AuthenticationExtractor} object
 * to actually build an {@code Authentication} from the exchange
 */
@Slf4j
@Component
public class AuthenticationExtractionWebFilter implements WebFilter {
    private final AuthenticationExtractor authenticationExtractor;

    public AuthenticationExtractionWebFilter(AuthenticationExtractor authenticationExtractor) {
        this.authenticationExtractor = authenticationExtractor;
    }

    /**
     * Tries to build authentication claims from the exchange and write them to {@link reactor.util.context.Context}
     * for potential consumption of downstream filters
     * <p>
     * <b>This filter never attempts to authenticate the claims</b>. Authentication of claims may be performed
     * by a downstream filter or may have been already performed outside of this application entirely by an
     * authentication server that issued an authentication token contained in the request.
     * In the latter case, the associated {@code AuthenticationExtractor} may check the signing key
     * of an extracted authentication token. At any rate, this filter is completely agnostic to such details
     * <p>
     * If the exchange doesn't contain extractable claims in any supported location, the filter simply forwards the request
     * down the filter chain
     * <p>
     * If the extraction of authentication claims fails, the method sets the {@code 401 Unauthorized} status to the response
     * and returns an empty {@code Mono}
     */
    @Override
    @SuppressWarnings("NullableProblems")
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return Mono.just(authenticationExtractor)
                .filter(extractor -> extractor.isSupportedSource(exchange))
                .switchIfEmpty(Mono.defer(() -> chain.filter(exchange).then(Mono.empty())))
                .flatMap(extractor -> extractor.tryExtractAuthentication(exchange))
                .flatMap(authentication -> chain.filter(exchange)
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication)))
                .onErrorResume(AuthenticationException.class,
                        t -> ResponseWriter.writeUnauthorizedResponse(exchange, t));
    }
}