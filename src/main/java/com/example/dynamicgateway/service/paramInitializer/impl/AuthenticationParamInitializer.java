package com.example.dynamicgateway.service.paramInitializer.impl;

import com.example.dynamicgateway.service.paramInitializer.ReplacingParamInitializer;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;

import java.util.Collection;

/**
 * A {@link ReplacingParamInitializer} that extracts parameter values from an {@link Authentication} object
 */
public abstract class AuthenticationParamInitializer extends ReplacingParamInitializer {
    @Override
    public Flux<?> getParamValues(ServerWebExchange exchange) {
        return exchange.getPrincipal()
                .flatMapIterable(principal ->
                        extractValuesFromAuthentication((Authentication) principal));
    }

    public abstract Collection<?> extractValuesFromAuthentication(Authentication authentication);
}
