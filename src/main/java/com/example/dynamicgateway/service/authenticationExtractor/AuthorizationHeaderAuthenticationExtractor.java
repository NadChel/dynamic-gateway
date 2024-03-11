package com.example.dynamicgateway.service.authenticationExtractor;

import com.example.dynamicgateway.exception.UnsupportedAuthenticationSourceException;
import com.example.dynamicgateway.model.authorizationHeader.AuthorizationHeader;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * A {@link HeaderAuthenticationExtractor} that extracts an {@code Authentication} from the request's {@code Authorization} header
 *
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc7235#section-4.2">RFC 7235</a>
 */
public interface AuthorizationHeaderAuthenticationExtractor extends HeaderAuthenticationExtractor {
    @Override
    default Mono<Authentication> doTryExtractAuthentication(HttpHeaders headers) {
        return Mono.just(headers)
                .map(h -> Objects.requireNonNull(h.getFirst(HttpHeaders.AUTHORIZATION)))
                .onErrorMap(NullPointerException.class,
                        t -> new UnsupportedAuthenticationSourceException("No Authorization header"))
                .map(AuthorizationHeader::fromString)
                .flatMap(this::doTryExtractAuthentication);
    }

    Mono<Authentication> doTryExtractAuthentication(AuthorizationHeader authorizationHeader);

    @Override
    default boolean areSupportedHeaders(HttpHeaders headers) {
        String header = headers.getFirst(HttpHeaders.AUTHORIZATION);
        return isSupportedAuthorizationHeader(AuthorizationHeader.fromString(header));
    }

    boolean isSupportedAuthorizationHeader(AuthorizationHeader header);
}
