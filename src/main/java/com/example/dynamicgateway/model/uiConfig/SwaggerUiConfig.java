package com.example.dynamicgateway.model.uiConfig;

import com.example.dynamicgateway.model.documentedApplication.SwaggerApplication;
import com.example.dynamicgateway.service.swaggerUiSupport.serializer.SwaggerUiConfigSerializer;
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
