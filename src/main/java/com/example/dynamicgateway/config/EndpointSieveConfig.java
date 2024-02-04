package com.example.dynamicgateway.config;

import com.example.dynamicgateway.model.gatewayMeta.GatewayMeta;
import com.example.dynamicgateway.service.endpointSieve.EndpointSieve;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.AntPathMatcher;

import java.util.Arrays;

@Configuration
public class EndpointSieveConfig {
    @Bean
    public EndpointSieve errorPathEndpointSieve(GatewayMeta gatewayMeta, AntPathMatcher antPathMatcher) {
        return endpoint -> Arrays.stream(gatewayMeta.getIgnoredPatterns())
                .noneMatch(ignoredPattern -> antPathMatcher.match(ignoredPattern, endpoint.getDetails().getPath()));
    }

    @Bean
    public AntPathMatcher antPathMatcher() {
        return new AntPathMatcher();
    }
}
