package com.example.dynamicgateway.service.authenticator;

import com.example.dynamicgateway.exception.UnsupportedAuthenticationSchemeException;
import com.example.dynamicgateway.model.authorizationHeader.AuthorizationHeader;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Primary
public class Authenticators implements Authenticator {
    private final List<? extends LeafAuthenticator> authenticators;

    public Authenticators(List<? extends LeafAuthenticator> authenticators) {
        this.authenticators = authenticators;
    }

    @Override
    public Authentication tryExtractAuthentication(AuthorizationHeader authorizationHeader) {
        for (LeafAuthenticator authenticator : authenticators)
            if (authenticator.hasSupportedScheme(authorizationHeader))
                return authenticator.extractAuthentication(authorizationHeader);
        throw new UnsupportedAuthenticationSchemeException(authorizationHeader.getScheme());
    }

}

