package com.example.dynamicgateway.exception;

import org.springframework.security.core.AuthenticationException;

public abstract class AuthenticationSchemeException extends AuthenticationException {
    public AuthenticationSchemeException(String msg) {
        super(msg);
    }
}
