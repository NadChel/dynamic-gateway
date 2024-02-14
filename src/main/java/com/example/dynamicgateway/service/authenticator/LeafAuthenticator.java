package com.example.dynamicgateway.service.authenticator;

import com.example.dynamicgateway.exception.UnsupportedAuthenticationSchemeException;
import com.example.dynamicgateway.model.authorizationHeader.AuthorizationHeader;
import org.springframework.security.core.Authentication;

/**
 * {@link Authenticator} that handles a specific authentication scheme
 */
interface LeafAuthenticator extends Authenticator {
    @Override
    default Authentication tryExtractAuthentication(AuthorizationHeader header) {
        requireSupportedScheme(header);
        return extractAuthentication(header);
    }

    default void requireSupportedScheme(AuthorizationHeader header) {
        if (!hasSupportedScheme(header))
            throw new UnsupportedAuthenticationSchemeException(header.getScheme());
    }

    default boolean hasSupportedScheme(AuthorizationHeader header) {
        return header.getScheme().equalsIgnoreCase(getSupportedScheme());
    }

    String getSupportedScheme();

    Authentication extractAuthentication(AuthorizationHeader header);
}
