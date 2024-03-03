package com.example.dynamicgateway.service.authenticationExtractor;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Object that tries to build an {@link Authentication} from a provided {@link ServerWebExchange}.
 *
 * <p>Functionally similar to {@link ServerAuthenticationConverter}
 */
public interface AuthenticationExtractor {
    /**
     * Attempts to extract an {@link Authentication} object from a passed-in {@code ServerWebExchange}
     *
     * @param exchange {@code ServerWebExchange} that may contain an extractable authentication
     * @return {@code Mono} that may contain an extracted {@code Authentication}
     */
    Mono<Authentication> tryExtractAuthentication(ServerWebExchange exchange);

    /**
     * Tests if this {@code AuthenticationExtractor} <em>may</em> extract an {@code Authentication} from
     * the provided {@code ServerWebExchange}
     *
     * <p>
     * <b>It provides no guarantee that an actual {@code Authentication} object will be extracted from the exchange.</b>
     * If the method returns {@code true}, it only means that the likelihood of a successful extraction is more than 0%.
     * For instance, it may mean that this {@code AuthenticationExtractor} discovers a specific request header whose value
     * it parses – which may not necessarily contain a valid token
     *
     * @param exchange {@code ServerWebExchange} that may be the source of an {@code Authentication}
     * @return {@code false} if {@code Authentication} extraction from the exchange by this {@code AuthenticationExtractor}
     * is impossible, {@code true} if it <em>may</em> result in a successful extraction
     */
    boolean isSupportedSource(ServerWebExchange exchange);
}
