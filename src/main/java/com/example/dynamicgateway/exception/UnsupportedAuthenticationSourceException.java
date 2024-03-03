package com.example.dynamicgateway.exception;

public class UnsupportedAuthenticationSourceException extends AuthenticationSourceException {
    public UnsupportedAuthenticationSourceException() {
        this("Could not extract authentication claims");
    }
    public UnsupportedAuthenticationSourceException(String msg) {
        super(msg);
    }
}
