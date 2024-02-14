package com.example.dynamicgateway.exception;

import java.text.MessageFormat;

public class UnsupportedAuthenticationSchemeException extends AuthenticationSchemeException {
    public UnsupportedAuthenticationSchemeException(String scheme) {
        super(MessageFormat.format("Unsupported authentication scheme: {0}", scheme));
    }
}
