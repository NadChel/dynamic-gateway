package com.example.dynamicgateway.controller;

import com.example.dynamicgateway.service.swaggerUiSupport.SwaggerUiSupport;
import io.swagger.v3.oas.models.OpenAPI;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class SwaggerDocController {
    private final SwaggerUiSupport uiSupport;

    @GetMapping("{application-name}/doc")
    public Mono<OpenAPI> getSwaggerAppDoc(@PathVariable("application-name") String applicationName) {
        return uiSupport.getSwaggerAppDoc(applicationName);
    }
}
