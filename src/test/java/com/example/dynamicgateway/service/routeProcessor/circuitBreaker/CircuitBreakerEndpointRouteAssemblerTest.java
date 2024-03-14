package com.example.dynamicgateway.service.routeProcessor.circuitBreaker;

import com.example.dynamicgateway.config.RouteAssemblerConfig;
import com.example.dynamicgateway.model.documentedEndpoint.DocumentedEndpoint;
import com.example.dynamicgateway.model.gatewayMeta.GatewayMeta;
import com.example.dynamicgateway.service.routeProcessor.EndpointRouteAssembler;
import com.example.dynamicgateway.testUtil.RouteBuilderUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.SpringCloudCircuitBreakerFilterFactory;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpResponse;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.reactive.config.EnableWebFlux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import static com.example.dynamicgateway.service.routeProcessor.circuitBreaker.CircuitBreakerEndpointRouteAssemblerTest.CircuitBreakerTestConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = CircuitBreakerTestConfig.class)
public class CircuitBreakerEndpointRouteAssemblerTest {
    static final Duration TIMEOUT = Duration.ofMillis(100);
    static final String appName = "some-app";
    @Autowired
    SpringCloudCircuitBreakerFilterFactory filterFactory;
    @Autowired
    GatewayMeta gatewayMeta;

    @Test
    void circuitBreakerEndpointRouteProcessor_addsFilterProvidedByCircuitBreakerFactoryToRoute() {
        RouteAssemblerConfig routeAssemblerConfig = new RouteAssemblerConfig(gatewayMeta);
        EndpointRouteAssembler circuitBreakerEndpointRouteProcessor =
                routeAssemblerConfig.circuitBreakerEndpointRouteAssembler(filterFactory);
        Route.AsyncBuilder routeBuilder = Route.async().id(UUID.randomUUID().toString());

        DocumentedEndpoint<?> endpointMock = mock(DocumentedEndpoint.class, RETURNS_DEEP_STUBS);
        given(endpointMock.getDeclaringApp().getName()).willReturn(appName);

        List<GatewayFilter> filters = RouteBuilderUtil.getFilters(routeBuilder);
        assumeThat(filters).isEmpty();

        circuitBreakerEndpointRouteProcessor.process(routeBuilder, endpointMock);

        assertThat(filters.size()).isEqualTo(1);
        GatewayFilter circuitBreakerFilter = filters.get(0);

        MockServerWebExchange exchange =
                MockServerWebExchange.builder(MockServerHttpRequest.get("/").build()).build();
        long marginOfErrorInMillis = 50;
        GatewayFilterChain slowChain = e ->
                Mono.delay(TIMEOUT.plusMillis(marginOfErrorInMillis)).then(Mono.empty());
        StepVerifier.create(circuitBreakerFilter.filter(exchange, slowChain))
                .verifyComplete();
        MockServerHttpResponse response = exchange.getResponse();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.GATEWAY_TIMEOUT);
        StepVerifier.create(response.getBodyAsString())
                .assertNext(this::assertExpectedMessage)
                .verifyComplete();
    }

    private void assertExpectedMessage(String fallbackMessage) {
        assertThat(fallbackMessage).containsPattern(Pattern.compile(
                appName + " (is )?(currently )?unavailable"));
    }

    @Configuration
    @EnableCircuitBreaker
    @EnableWebFlux
    static class CircuitBreakerTestConfig {
        @Bean
        GatewayMeta testGatewayMeta() {
            GatewayMeta gatewayMeta = new GatewayMeta();
            gatewayMeta.setTimeout(TIMEOUT);
            return gatewayMeta;
        }
    }
}
