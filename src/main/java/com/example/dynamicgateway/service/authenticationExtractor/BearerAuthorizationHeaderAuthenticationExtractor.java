package com.example.dynamicgateway.service.authenticationExtractor;

import com.example.dynamicgateway.exception.UnsupportedAuthenticationSchemeException;
import com.example.dynamicgateway.model.authorizationHeader.AuthorizationHeader;
import org.springframework.security.core.Authentication;
import reactor.core.publisher.Mono;

/**
 * An {@link AuthorizationHeaderAuthenticationExtractor} that extracts an {@code Authentication} from a {@code bearer}
 * token contained in the header
 *
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc6750">RFC 6750</a>
 */
public interface BearerAuthorizationHeaderAuthenticationExtractor extends AuthorizationHeaderAuthenticationExtractor {
    @Override
    default Mono<Authentication> doTryExtractAuthentication(AuthorizationHeader authorizationHeader) {
        return Mono.just(authorizationHeader)
                .filter(this::isSupportedAuthorizationHeader)
                .switchIfEmpty(Mono.error(
                        new UnsupportedAuthenticationSchemeException(authorizationHeader.getScheme())))
                .map(AuthorizationHeader::getCredentials)
                .flatMap(this::doTryExtractAuthentication);
    }

    Mono<Authentication> doTryExtractAuthentication(String bearerToken);

    @Override
    default boolean isSupportedAuthorizationHeader(AuthorizationHeader header) {
        return header.getScheme().equalsIgnoreCase(AuthorizationHeader.BEARER_SPACE.trim());
    }
}
