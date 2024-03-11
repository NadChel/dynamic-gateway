package com.example.dynamicgateway.service.routeProcessor.circuitBreaker;

import com.example.dynamicgateway.config.MyCircuitBreakerConfig;
import com.example.dynamicgateway.controller.FallbackController;
import io.github.resilience4j.springboot3.circuitbreaker.autoconfigure.CircuitBreakerAutoConfiguration;
import io.github.resilience4j.springboot3.timelimiter.autoconfigure.TimeLimiterConfigurationOnMissingBean;
import io.github.resilience4j.springboot3.timelimiter.autoconfigure.TimeLimiterProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JAutoConfiguration;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigurationProperties;
import org.springframework.cloud.gateway.config.GatewayResilience4JCircuitBreakerAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@EnableConfigurationProperties({
        TimeLimiterProperties.class,
        Resilience4JConfigurationProperties.class
})
@Import({TimeLimiterConfigurationOnMissingBean.class,
        CircuitBreakerAutoConfiguration.class,
        ReactiveResilience4JAutoConfiguration.class,
        GatewayResilience4JCircuitBreakerAutoConfiguration.class,
        MyCircuitBreakerConfig.class,
        FallbackController.class})
public @interface EnableCircuitBreaker {
}
