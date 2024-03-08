package com.example.dynamicgateway.controller;

import com.example.dynamicgateway.controller.config.EnableMockAuthentication;
import com.example.dynamicgateway.model.documentedApplication.SwaggerApplication;
import com.example.dynamicgateway.model.uiConfig.SwaggerUiConfig;
import com.example.dynamicgateway.service.swaggerUiSupport.SwaggerUiSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@WebFluxTest(controllers = SwaggerUiConfigController.class)
@EnableMockAuthentication
@ActiveProfiles("test")
class SwaggerUiConfigControllerTest {
    @Autowired
    WebTestClient testClient;
    @MockBean
    SwaggerUiSupport swaggerUiSupportMock;
    @Value("${springdoc.swagger-ui.config-url}")
    String configUrl;

    @Test
    void testGetConfig() {
        SwaggerApplication application1 = mock(SwaggerApplication.class);
        SwaggerApplication application2 = mock(SwaggerApplication.class);
        given(application1.getName()).willReturn("app-one");
        given(application2.getName()).willReturn("app-two");
        SwaggerUiConfig swaggerUiConfig = new SwaggerUiConfig(List.of(application1, application2));
        given(swaggerUiSupportMock.getSwaggerUiConfig()).willReturn(Mono.just(swaggerUiConfig));

        testClient
                .get()
                .uri(configUrl)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                            "urls": [
                                {
                                    "name": "app-one",
                                    "url": "/doc/app-one"
                                },
                                {
                                    "name": "app-two",
                                    "url": "/doc/app-two"
                                }
                            ]
                        }
                        """);
    }
}