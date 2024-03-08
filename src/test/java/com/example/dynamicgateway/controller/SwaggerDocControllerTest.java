package com.example.dynamicgateway.controller;

import com.example.dynamicgateway.controller.config.EnableMockAuthentication;
import com.example.dynamicgateway.service.swaggerUiSupport.SwaggerUiSupport;
import com.example.dynamicgateway.testUtil.SwaggerParseResultGenerator;
import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.BDDMockito.given;

@WebFluxTest(controllers = SwaggerDocController.class)
@EnableMockAuthentication
@ActiveProfiles("test")
class SwaggerDocControllerTest {
    @Autowired
    WebTestClient testClient;
    @MockBean
    SwaggerUiSupport swaggerUiSupportMock;
    @Value("${springdoc.swagger-ui.config-url}")
    String configUrl;

    @Test
    void getSwaggerAppDoc() {
        String appName = "some-app";
        OpenAPI openAPI = SwaggerParseResultGenerator.empty().getOpenAPI();
        given(swaggerUiSupportMock.getSwaggerAppDoc(appName)).willReturn(Mono.just(openAPI));

        testClient
                .get()
                .uri("/doc/" + appName)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(OpenAPI.class)
                .isEqualTo(openAPI);
    }
}