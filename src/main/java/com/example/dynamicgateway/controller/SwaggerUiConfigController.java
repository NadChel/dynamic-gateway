package com.example.dynamicgateway.controller;

import com.example.dynamicgateway.model.uiConfig.SwaggerUiConfig;
import com.example.dynamicgateway.service.swaggerUiSupport.SwaggerUiSupport;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class SwaggerUiConfigController {
    private final SwaggerUiSupport uiSupport;

    public SwaggerUiConfigController(SwaggerUiSupport uiSupport) {
        this.uiSupport = uiSupport;
    }

    @GetMapping("/swagger-ui/config")
    public Mono<SwaggerUiConfig> getConfig() {
        return uiSupport.getSwaggerUiConfig();
    }
}
