package com.example.dynamicgateway.model.uiConfig;

import com.example.dynamicgateway.model.documentedApplication.SwaggerApplication;
import com.example.dynamicgateway.service.swaggerUiSupport.serializer.SwaggerUiConfigSerializer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;

import java.util.Collection;

@Getter
public class SwaggerUiConfig {
    @JsonProperty("urls")
    @JsonSerialize(contentUsing = SwaggerUiConfigSerializer.class)
    private Collection<SwaggerApplication> swaggerApplications;

    private SwaggerUiConfig(Collection<SwaggerApplication> swaggerApplications) {
        this.swaggerApplications = swaggerApplications;
    }

    public static SwaggerUiConfig from(Collection<SwaggerApplication> swaggerApplications) {
        return new SwaggerUiConfig(swaggerApplications);
    }
}
