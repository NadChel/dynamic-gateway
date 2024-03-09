package com.example.dynamicgateway.controller;

import com.example.dynamicgateway.service.swaggerUiSupport.SwaggerUiSupport;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class SwaggerDocController {
    private final SwaggerUiSupport uiSupport;

    public SwaggerDocController(SwaggerUiSupport uiSupport) {
        this.uiSupport = uiSupport;
    }

    @GetMapping("/doc/{application-name}")
    public Mono<OpenAPI> getSwaggerAppDoc(@PathVariable("application-name") String applicationName) {
        return uiSupport.getSwaggerAppDoc(applicationName);
    }
}
