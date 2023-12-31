package com.example.dynamicgateway.controller;

import com.example.dynamicgateway.model.uiConfig.SwaggerUiConfig;
import com.example.dynamicgateway.service.swaggerUiSupport.SwaggerUiSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class SwaggerUiConfigController {
    private final SwaggerUiSupport uiSupport;

    @GetMapping("/swagger-ui/config")
    public Mono<SwaggerUiConfig> getConfig() {
        return uiSupport.getSwaggerUiConfig();
    }
}
