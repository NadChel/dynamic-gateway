package com.example.dynamicgateway.service.paramInitializer.impl;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * An {@link AuthenticationParamInitializer} that retrieves the authorities of an
 * {@code Authentication} instance
 */
@Component
public class RolesParamInitializer extends AuthenticationParamInitializer {
    public String getParamName() {
        return "roles";
    }

    @Override
    public Collection<?> extractValuesFromAuthentication(Authentication authentication) {
        return authentication.getAuthorities();
    }
}
