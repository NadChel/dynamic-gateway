package com.example.dynamicgateway.service.authenticationExtractor;

import com.example.dynamicgateway.model.authorizationHeader.AuthorizationHeader;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import reactor.core.publisher.Mono;

/**
 * A {@link HeaderAuthenticationExtractor} that extracts an {@code Authentication} from the request's {@code Authorization} header
 *
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc7235#section-4.2">RFC 7235</a>
 */
public interface AuthorizationHeaderAuthenticationExtractor extends HeaderAuthenticationExtractor {
    @Override
    default Mono<Authentication> tryExtractAuthentication(HttpHeaders headers) {
        return Mono.just(headers)
                .mapNotNull(h -> h.getFirst(HttpHeaders.AUTHORIZATION))
                .map(AuthorizationHeader::new)
                .flatMap(this::tryExtractAuthentication);
    }

    Mono<Authentication> tryExtractAuthentication(AuthorizationHeader authorizationHeader);

    @Override
    default boolean areSupportedHeaders(HttpHeaders headers) {
        String header = headers.getFirst(HttpHeaders.AUTHORIZATION);
        return isSupportedAuthorizationHeader(new AuthorizationHeader(header));
    }

    boolean isSupportedAuthorizationHeader(AuthorizationHeader header);
}
