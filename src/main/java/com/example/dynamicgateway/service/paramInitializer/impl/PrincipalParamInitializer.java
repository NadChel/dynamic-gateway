package com.example.dynamicgateway.service.paramInitializer.impl;

import lombok.Getter;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

@Component
@Getter
public class PrincipalParamInitializer extends AuthenticationParamInitializer {
    private final String paramName = "principal";

    @Override
    public Collection<?> extractValuesFromAuthentication(Authentication authentication) {
        return List.of(authentication.getName());
    }
}
