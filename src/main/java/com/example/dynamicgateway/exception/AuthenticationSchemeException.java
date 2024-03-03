package com.example.dynamicgateway.exception;

public abstract class AuthenticationSchemeException extends AuthenticationSourceException {
    public AuthenticationSchemeException(String msg) {
        super(msg);
    }
}
