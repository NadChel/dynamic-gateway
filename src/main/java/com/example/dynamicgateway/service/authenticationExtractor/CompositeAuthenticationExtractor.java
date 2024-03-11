package com.example.dynamicgateway.service.authenticationExtractor;

import com.example.dynamicgateway.exception.UnsupportedAuthenticationSourceException;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * An {@link AuthenticationExtractor} that delegates actual extraction to one of its aggregated {@code AuthenticationExtractor}s
 */
@Component
@Primary
public class CompositeAuthenticationExtractor implements AuthenticationExtractor {
    private final List<? extends AuthenticationExtractor> authenticationExtractors;

    public CompositeAuthenticationExtractor(List<? extends AuthenticationExtractor> authenticationExtractors) {
        this.authenticationExtractors = authenticationExtractors;
    }

    /**
     * Finds the first delegate that supports the provided exchange and delegates extraction to it
     *
     * @return {@code Mono} of {@code Authentication} on successful extraction,
     * {@code Mono} of {@link UnsupportedAuthenticationSourceException} if none of the delegates
     * supports the source
     * @see AuthenticationExtractor#isSupportedSource
     */
    @Override
    public Mono<Authentication> doTryExtractAuthentication(ServerWebExchange exchange) {
        return Flux.fromIterable(authenticationExtractors)
                .filter(extractor -> extractor.isSupportedSource(exchange))
                .switchIfEmpty(Mono.error(new UnsupportedAuthenticationSourceException()))
                .next()
                .flatMap(extractor -> extractor.doTryExtractAuthentication(exchange));
    }

    /**
     * Iterates over the delegates to see whether at least one of them supports {@code Authentication}
     * extraction from the provided exchange
     *
     * @return {@code true} if such a delegate is found, {@code false} otherwise
     */
    @Override
    public boolean isSupportedSource(ServerWebExchange exchange) {
        return authenticationExtractors.stream()
                .anyMatch(extractor -> extractor.isSupportedSource(exchange));
    }
}

