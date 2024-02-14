package com.example.dynamicgateway.config;

import com.example.dynamicgateway.service.authenticator.Authenticator;
import com.example.dynamicgateway.service.authenticator.Authenticators;
import com.example.dynamicgateway.service.authenticator.BearerAuthenticator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/*
 * Returned Authenticator beans are used solely as an input to Authenticators constructor,
 * hence empty credentials
 */
@Configuration
public class AuthenticatorConfig {
    @Bean
    public Authenticator bearerAuthenticator() {
        return new BearerAuthenticator("");
    }

    @Bean
    public Authenticators authenticators(List<? extends Authenticator> authenticators) {
        return new Authenticators(authenticators);
    }
}
