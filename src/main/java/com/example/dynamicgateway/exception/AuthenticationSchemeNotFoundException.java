package com.example.dynamicgateway.exception;

public class AuthenticationSchemeNotFoundException extends AuthenticationSchemeException {
    public AuthenticationSchemeNotFoundException() {
        super("Authorization header should specify authentication scheme: <scheme> <credentials>");
    }
}
