package com.example.dynamicgateway.service.authenticator;

import com.example.dynamicgateway.model.authorizationHeader.AuthorizationHeader;
import org.springframework.security.core.Authentication;

public abstract class Authenticator {
    protected final String credentials;

    protected Authenticator(String credentials) {
        this.credentials = credentials;
    }

    protected Authenticator(AuthorizationHeader authorizationHeader) {
        this(authorizationHeader.getCredentials());
    }

    public abstract String getHandledScheme();

    public abstract Authentication buildAuthentication();
}
