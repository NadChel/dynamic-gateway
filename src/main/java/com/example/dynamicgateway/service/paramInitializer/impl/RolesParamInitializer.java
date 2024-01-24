package com.example.dynamicgateway.service.paramInitializer.impl;

import lombok.Getter;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
@Getter
public class RolesParamInitializer extends AuthenticationParamInitializer {
    private final String paramName = "roles";

    @Override
    public Collection<?> extractValuesFromAuthentication(Authentication authentication) {
        return authentication.getAuthorities();
    }
}
