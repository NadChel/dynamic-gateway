package by.afinny.apigateway.config;

import by.afinny.apigateway.client.ApplicationDocClient;
import by.afinny.apigateway.client.SwaggerClientConfigurer;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ClientConfig {
    @Bean
    @LoadBalanced
    public WebClient.Builder balancedWebClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    public ApplicationDocClient<SwaggerParseResult> applicationDocClient() {
        return SwaggerClientConfigurer.configure(balancedWebClientBuilder().build()).build();
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return objectMapper;
    }
}
