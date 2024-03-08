package com.example.dynamicgateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveUserDetailsServiceAutoConfiguration;

@SpringBootApplication(exclude = ReactiveUserDetailsServiceAutoConfiguration.class)
public class DynamicGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(DynamicGatewayApplication.class, args);
    }
}
