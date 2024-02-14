package com.example.dynamicgateway.config;

import com.example.dynamicgateway.service.swaggerUiSupport.serializer.SecuritySchemeTypeSerializer;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {
    @Bean
    public ObjectMapper customizedObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.registerModule(securitySchemeModule());
        return objectMapper;
    }

    @Bean
    public Module securitySchemeModule() {
        SimpleModule securitySchemeModule = new SimpleModule();
        securitySchemeModule.addSerializer(securitySchemeTypeSerializer());
        return securitySchemeModule;
    }

    @Bean
    public JsonSerializer<SecurityScheme.Type> securitySchemeTypeSerializer() {
        return new SecuritySchemeTypeSerializer();
    }
}
