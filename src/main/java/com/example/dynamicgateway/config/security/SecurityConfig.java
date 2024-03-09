package com.example.dynamicgateway.config.security;

import com.example.dynamicgateway.config.security.filter.AuthenticationExtractionWebFilter;
import com.example.dynamicgateway.model.gatewayMeta.GatewayMeta;
import com.example.dynamicgateway.util.ResponseWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
    private final AuthenticationExtractionWebFilter authenticationExtractionWebFilter;
    private final GatewayMeta gatewayMeta;

    public SecurityConfig(AuthenticationExtractionWebFilter authenticationExtractionWebFilter,
                          GatewayMeta gatewayMeta) {
        this.authenticationExtractionWebFilter = authenticationExtractionWebFilter;
        this.gatewayMeta = gatewayMeta;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .addFilterBefore(authenticationExtractionWebFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(gatewayMeta.getPublicPatterns().toArray(new String[0])).permitAll()
                        .anyExchange().authenticated())
                .exceptionHandling(exceptionHandlingSpec -> exceptionHandlingSpec
                        .authenticationEntryPoint(ResponseWriter::writeUnauthorizedResponse)
                        .accessDeniedHandler(ResponseWriter::writeUnauthorizedResponse))
                .build();
    }
}