package com.example.dynamicgateway.service.authenticator;

import com.example.dynamicgateway.model.authorizationHeader.AuthorizationHeader;
import lombok.SneakyThrows;

import java.util.List;
import java.util.Optional;

public class Authenticators {
    private final List<? extends Authenticator> authenticators;

    public Authenticators(List<? extends Authenticator> authenticators) {
        this.authenticators = authenticators;
    }

    public Optional<Authenticator> findAuthenticatorFor(AuthorizationHeader authorizationHeader) {
        String scheme = authorizationHeader.getScheme();
        Optional<? extends Class<? extends Authenticator>> optionalAuthenticatorClass = authenticators.stream()
                .filter(authenticator -> authenticator.getHandledScheme().equalsIgnoreCase(scheme))
                .map(authenticator -> authenticator.getClass())
                .findFirst();
        return optionalAuthenticatorClass.map(authClass -> instantiate(authClass, authorizationHeader));
    }

    @SneakyThrows
    private Authenticator instantiate(Class<? extends Authenticator> authenticatorClass,
                                                AuthorizationHeader header) {
        return authenticatorClass.getConstructor(AuthorizationHeader.class)
                .newInstance(header);
    }
}

