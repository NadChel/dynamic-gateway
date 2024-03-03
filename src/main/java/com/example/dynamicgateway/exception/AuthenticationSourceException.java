package com.example.dynamicgateway.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * A superclass for exceptions relating to invalid {@code Authentication} sources.
 * A raised exception of this type signals that authentication claims could not be obtained
 */
public abstract class AuthenticationSourceException extends AuthenticationException {
    public AuthenticationSourceException(String msg) {
        super(msg);
    }
}
