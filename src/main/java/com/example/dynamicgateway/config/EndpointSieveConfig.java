package com.example.dynamicgateway.config;

import com.example.dynamicgateway.model.gatewayMeta.GatewayMeta;
import com.example.dynamicgateway.service.sieve.EndpointSieve;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.AntPathMatcher;

@Configuration
public class EndpointSieveConfig {
    @Bean
    public EndpointSieve errorPathEndpointSieve(GatewayMeta gatewayMeta, AntPathMatcher antPathMatcher) {
        return endpoint -> gatewayMeta.getIgnoredPatterns().stream()
                .noneMatch(ignoredPattern -> antPathMatcher.match(ignoredPattern, endpoint.getDetails().getPath()));
    }

    @Bean
    public AntPathMatcher antPathMatcher() {
        return new AntPathMatcher();
    }
}
