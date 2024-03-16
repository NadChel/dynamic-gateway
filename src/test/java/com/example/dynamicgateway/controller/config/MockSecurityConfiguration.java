package com.example.dynamicgateway.controller.config;

import com.example.dynamicgateway.config.security.SecurityConfig;
import com.example.dynamicgateway.model.gatewayMeta.GatewayMeta;
import com.example.dynamicgateway.service.authenticationExtractor.AuthenticationExtractor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Configuration
@Import(SecurityConfig.class)
@EnableConfigurationProperties(GatewayMeta.class)
public class MockSecurityConfiguration {
    @Bean
    public AuthenticationExtractor mockAuthenticationExtractor() {
        return new AuthenticationExtractor() {
            @Override
            public Mono<Authentication> doTryExtractAuthentication(ServerWebExchange exchange) {
                TestingAuthenticationToken token = new TestingAuthenticationToken(
                        "user", "password",
                        AuthorityUtils.createAuthorityList("user")
                );
                return Mono.just(token);
            }

            @Override
            public boolean isSupportedSource(ServerWebExchange exchange) {
                return true;
            }
        };
    }
}
