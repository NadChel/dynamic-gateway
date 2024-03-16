package com.example.dynamicgateway.service.authenticationExtractor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Object that tries to build an {@link Authentication} from a provided {@link ServerWebExchange}
 * <p>
 * Implementations <b>should not</b> attempt to perform any authentication and are instead expected
 * to return authentication claims as is
 * <p>
 * This type is functionally similar to {@link ServerAuthenticationConverter}
 */
public interface AuthenticationExtractor {
    /**
     * Attempts to extract an {@link Authentication} object from a passed-in {@code ServerWebExchange}
     * <p>
     * Implementations are not expected to override this method since it performs exception translation.
     * Instead, they should override {@link AuthenticationExtractor#doTryExtractAuthentication(ServerWebExchange)}
     *
     * @param exchange a {@code ServerWebExchange} that may contain extractable authentication claims
     * @return a {@code Mono} of an extracted {@code Authentication} or a {@code Mono} of an
     * {@link AuthenticationException} if extraction fails
     */
    default Mono<Authentication> tryExtractAuthentication(ServerWebExchange exchange) {
        return doTryExtractAuthentication(exchange)
                .onErrorMap(this::isNotAuthenticationException,
                        this::wrapInAuthenticationException);
    }

    Mono<Authentication> doTryExtractAuthentication(ServerWebExchange exchange);

    private boolean isNotAuthenticationException(Throwable throwable) {
        return !(throwable instanceof AuthenticationException);
    }

    private AuthenticationException wrapInAuthenticationException(Throwable throwable) {
        return new AuthenticationException("Extraction of authentication claims failed", throwable) {
        };
    }

    /**
     * Tests if this {@code AuthenticationExtractor} <em>may</em> extract an {@code Authentication} from
     * the provided {@code ServerWebExchange}
     * <p>
     * <b>If this method returns {@code true}, it does not guarantee that an {@code Authentication} object will be extracted from the exchange.</b>
     * Instead, it only means that the likelihood of a successful extraction is more than 0%.
     * For instance, it may mean that this {@code AuthenticationExtractor} discovers a specific request header whose value
     * it parses – which may not necessarily contain a valid token
     *
     * @param exchange {@code ServerWebExchange} that may be the source of an {@code Authentication}
     * @return {@code false} if {@code Authentication} extraction from the exchange by this {@code AuthenticationExtractor}
     * is impossible, {@code true} if it <em>may</em> result in a successful extraction
     */
    boolean isSupportedSource(ServerWebExchange exchange);
}
