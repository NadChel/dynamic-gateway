package com.example.dynamicgateway.config;

import com.example.dynamicgateway.service.endpointSieve.DiscoverableApplicationSieve;
import com.netflix.discovery.EurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DiscoverableApplicationSieveConfig {
    @Bean
    public DiscoverableApplicationSieve selfExclusionDiscoverableApplicationSieve(EurekaClient eurekaClient) {
        return discoverableApp -> {
            String nameOfThisApplication = eurekaClient.getApplicationInfoManager()
                    .getInfo()
                    .getAppName();
            return !nameOfThisApplication.equals(discoverableApp.getName());
        };
    }
}
