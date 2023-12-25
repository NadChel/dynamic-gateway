package by.afinny.apigateway.controller;

import by.afinny.apigateway.model.uiConfig.SwaggerUiConfig;
import by.afinny.apigateway.service.swaggerUiSupport.SwaggerUiSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class SwaggerUiConfigController {
    private final SwaggerUiSupport uiSupport;

    @GetMapping("/swagger-ui-config")
    public Mono<SwaggerUiConfig> getConfig() {
        return uiSupport.getSwaggerUiConfig();
    }
}
