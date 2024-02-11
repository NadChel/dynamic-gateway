package com.example.dynamicgateway.config;

import com.example.dynamicgateway.service.applicationDocClient.ApplicationDocClient;
import com.example.dynamicgateway.service.applicationDocClient.SwaggerClientConfigurer;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ClientConfig {
    @Bean
    @LoadBalanced
    public WebClient.Builder balancedWebClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    public ApplicationDocClient<SwaggerParseResult> applicationDocClient() {
        return SwaggerClientConfigurer.configure(balancedWebClientBuilder().build()).build();
    }
}
