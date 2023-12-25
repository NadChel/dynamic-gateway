package by.afinny.apigateway.service.swaggerUiSupport;

import by.afinny.apigateway.model.uiConfig.SwaggerUiConfig;
import io.swagger.v3.oas.models.OpenAPI;
import reactor.core.publisher.Mono;

/**
 * Interface for marking utilities that simplify Swagger UI integration
 */
public interface SwaggerUiSupport {
    Mono<SwaggerUiConfig> getSwaggerUiConfig();

    Mono<OpenAPI> getSwaggerAppDoc(String appName);
}
