package com.example.dynamicgateway.service.paramInitializer;

import com.example.dynamicgateway.testUtil.RouteBuilderUtil;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ParamInitializerTest {
    private final List<Integer> testParamValues = List.of(1, 2, 3);
    private final Route.AsyncBuilder routeBuilder = Route.async();
    private final ParamInitializer paramInitializer = new ParamInitializer() {
        @Override
        public String getParamName() {
            return "param";
        }

        @Override
        public Flux<?> getParamValues(ServerWebExchange exchange) {
            return Flux.fromIterable(testParamValues);
        }
    };

    @Test
    void whenInitializeInvoked_filterAdded() {
        assumeThat(RouteBuilderUtil.getFilters(routeBuilder)).isEmpty();

        paramInitializer.initialize(routeBuilder);

        List<GatewayFilter> filters = RouteBuilderUtil.getFilters(routeBuilder);

        assertThat(filters).hasSize(1);
    }

    @Test
    void whenInitializeInvoked_filterCorrect() {
        assumeThat(RouteBuilderUtil.getFilters(routeBuilder)).asList().isEmpty();

        paramInitializer.initialize(routeBuilder);

        List<GatewayFilter> filters = RouteBuilderUtil.getFilters(routeBuilder);

        assumeThat(filters).asList().hasSize(1);

        checkFilter(filters.get(0));
    }

    private void checkFilter(GatewayFilter filter) {
        MockServerHttpRequest requestMock = MockServerHttpRequest.get("/test-path").build();

        assumeThat(requestMock.getQueryParams().get(paramInitializer.getParamName())).isNull();

        ServerWebExchange exchangeMock = MockServerWebExchange.from(requestMock);

        ArgumentCaptor<ServerWebExchange> exchangeCaptor = ArgumentCaptor.forClass(ServerWebExchange.class);

        GatewayFilterChain chainMock = mock(GatewayFilterChain.class);
        when(chainMock.filter(any())).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchangeMock, chainMock))
                .expectComplete()
                .verify();

        verify(chainMock).filter(exchangeCaptor.capture());

        List<String> actualParamValues = exchangeCaptor.getValue().getRequest().getQueryParams().get(paramInitializer.getParamName());

        assertThat(actualParamValues)
                .asList()
                .containsExactlyInAnyOrderElementsOf(testParamValues.stream().map(String::valueOf).toList());
    }
}