package com.example.dynamicgateway.exception;

public class AuthenticationSchemeNotFoundException extends AuthenticationSchemeException {
    public AuthenticationSchemeNotFoundException() {
        this("An authentication scheme is expected");
    }

    public AuthenticationSchemeNotFoundException(String message) {
        super(message);
    }
}
