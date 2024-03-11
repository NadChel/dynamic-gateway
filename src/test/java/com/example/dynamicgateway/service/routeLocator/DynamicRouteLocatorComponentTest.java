package com.example.dynamicgateway.service.routeLocator;

import com.example.dynamicgateway.config.RouteProcessorConfig;
import com.example.dynamicgateway.events.DocumentedEndpointFoundEvent;
import com.example.dynamicgateway.model.documentedEndpoint.DocumentedEndpoint;
import com.example.dynamicgateway.model.gatewayMeta.GatewayMeta;
import com.example.dynamicgateway.service.paramInitializer.ParamInitializers;
import com.example.dynamicgateway.service.routeLocator.DynamicRouteLocatorComponentTest.DynamicRouteLocatorComponentTestConfig;
import com.example.dynamicgateway.service.routeProcessor.EndpointRouteProcessor;
import com.example.dynamicgateway.testModel.SwaggerEndpointStub;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.factory.SpringCloudCircuitBreakerFilterFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = DynamicRouteLocatorComponentTestConfig.class)
public class DynamicRouteLocatorComponentTest {
    @Autowired
    List<EndpointRouteProcessor> routeProcessors;

    @Test
    void testWhenBuildingRoute_orderOfInjectedRouteProcessorsDoesntMatter() {
        DynamicRouteLocator routeLocator = new DynamicRouteLocator(routeProcessors);
        for (int i = 0; i < 10; i++) {
            Collections.shuffle(routeProcessors);
            DocumentedEndpoint<?> endpoint = SwaggerEndpointStub.builder().path("/" + UUID.randomUUID()).build();
            DocumentedEndpointFoundEvent event = new DocumentedEndpointFoundEvent(endpoint, this);
            assertThatCode(() -> routeLocator.onDocumentedEndpointFoundEvent(event)).doesNotThrowAnyException();
        }
    }

    @Configuration
    @Import(RouteProcessorConfig.class)
    public static class DynamicRouteLocatorComponentTestConfig {
        @Bean
        GatewayMeta gatewayMeta() {
            GatewayMeta gatewayMeta = new GatewayMeta();
            gatewayMeta.setVersionPrefix("/api/v1");
            gatewayMeta.setIgnoredPrefixes(Collections.singletonList("/auth"));
            return gatewayMeta;
        }

        @Bean
        ParamInitializers paramInitializers() {
            return new ParamInitializers(Collections.emptyList());
        }

        @Bean
        SpringCloudCircuitBreakerFilterFactory circuitBreakerFilterFactory() {
            return mock(SpringCloudCircuitBreakerFilterFactory.class);
        }
    }
}
