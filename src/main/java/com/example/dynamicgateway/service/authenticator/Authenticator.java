package com.example.dynamicgateway.service.authenticator;

import com.example.dynamicgateway.exception.UnsupportedAuthenticationSchemeException;
import com.example.dynamicgateway.model.authorizationHeader.AuthorizationHeader;
import org.springframework.security.core.Authentication;

/**
 * Object that tries to build an {@link Authentication} based on a provided {@link AuthorizationHeader}
 */
public interface Authenticator {
    /**
     * Attempts to parse {@link AuthorizationHeader} and build an {@link Authentication} object
     *
     * @param header {@code Authorization} header that may contain an extractable authentication
     * @return {@code Authentication} extracted from the header
     * @throws UnsupportedAuthenticationSchemeException if the header starts with an unsupported authentication scheme
     */
    Authentication tryExtractAuthentication(AuthorizationHeader header);
}
