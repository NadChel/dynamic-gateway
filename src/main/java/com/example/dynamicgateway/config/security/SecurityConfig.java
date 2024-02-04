package com.example.dynamicgateway.config.security;

import com.example.dynamicgateway.config.security.filter.JwtAuthorizationFilter;
import com.example.dynamicgateway.model.gatewayMeta.GatewayMeta;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.server.WebFilter;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
    private final GatewayMeta gatewayMeta;

    public SecurityConfig(GatewayMeta gatewayMeta) {
        this.gatewayMeta = gatewayMeta;
    }
    @Bean
    public SecurityWebFilterChain filterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(gatewayMeta.getPublicPatterns()).permitAll()
                        .anyExchange().authenticated()
                )
                .addFilterBefore(jwtAuthorizationFilter(), SecurityWebFiltersOrder.AUTHORIZATION)
                .exceptionHandling(exceptionHandlingSpec -> exceptionHandlingSpec
                        .authenticationEntryPoint((exchange, ex) -> Mono.fromRunnable(
                                () -> exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED)
                        )))
                .cors(corsSpec -> corsSpec.configurationSource(corsConfiguration()))
                .build();
    }

    private CorsConfigurationSource corsConfiguration() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedMethods(Collections.singletonList("*"));
        config.setAllowCredentials(true);
        config.setAllowedHeaders(Collections.singletonList("*"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setMaxAge(Duration.ofHours(1));
        config.setAllowedOriginPatterns(List.of("*"));
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public WebFilter jwtAuthorizationFilter() {
        return new JwtAuthorizationFilter();
    }
}