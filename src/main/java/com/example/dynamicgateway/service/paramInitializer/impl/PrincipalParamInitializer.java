package com.example.dynamicgateway.service.paramInitializer.impl;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

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
