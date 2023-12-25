package by.afinny.apigateway.model.uiConfig;

import by.afinny.apigateway.model.documentedApplication.SwaggerApplication;
import by.afinny.apigateway.service.swaggerUiSupport.SwaggerUiConfigSerializer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Collection;

@NoArgsConstructor
@Getter
public class SwaggerUiConfig {
    @JsonProperty("urls")
    @JsonSerialize(contentUsing = SwaggerUiConfigSerializer.class)
    private Collection<SwaggerApplication> swaggerApplications;

    public SwaggerUiConfig(Collection<SwaggerApplication> swaggerApplications) {
        this.swaggerApplications = swaggerApplications;
    }

    public static SwaggerUiConfig from(Collection<SwaggerApplication> swaggerApplications) {
        return new SwaggerUiConfig(swaggerApplications);
    }
}
