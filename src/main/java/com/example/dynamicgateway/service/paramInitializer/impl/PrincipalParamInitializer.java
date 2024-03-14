package com.example.dynamicgateway.service.paramInitializer.impl;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

/**
 * An {@link AuthenticationParamInitializer} that retrieves the name of the principal
 */
@Component
public class PrincipalParamInitializer extends AuthenticationParamInitializer {
    @Override
    public String getParamName() {
        return "principal";
    }

    @Override
    public Collection<?> extractValuesFromAuthentication(Authentication authentication) {
        return List.of(authentication.getName());
    }
}
