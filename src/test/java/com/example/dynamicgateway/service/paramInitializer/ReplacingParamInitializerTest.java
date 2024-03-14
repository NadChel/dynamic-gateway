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
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.given;

class ReplacingParamInitializerTest {
    private final List<Integer> testParamValues = List.of(1, 2, 3);
    private final Route.AsyncBuilder routeBuilder = Route.async();
    private final ParamInitializer paramInitializer = new ReplacingParamInitializer() {
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

        paramInitializer.addInitializingFilter(routeBuilder);

        List<GatewayFilter> filters = RouteBuilderUtil.getFilters(routeBuilder);
        assertThat(filters).hasSize(1);
    }

    @Test
    void whenInitializeInvoked_filterCorrect() {
        assumeThat(RouteBuilderUtil.getFilters(routeBuilder)).asList().isEmpty();

        paramInitializer.addInitializingFilter(routeBuilder);

        List<GatewayFilter> filters = RouteBuilderUtil.getFilters(routeBuilder);
        assumeThat(filters).asList().hasSize(1);

        assertFilterReplacesOldParamsOfName(filters.get(0));
    }

    private void assertFilterReplacesOldParamsOfName(GatewayFilter filter) {
        MockServerHttpRequest requestMock = MockServerHttpRequest.get("/test-path")
                .queryParam(paramInitializer.getParamName(),
                        "these", "values", "should", "be", "replaced")
                .build();

        ServerWebExchange exchangeMock = MockServerWebExchange.from(requestMock);

        ArgumentCaptor<ServerWebExchange> exchangeCaptor = ArgumentCaptor.forClass(ServerWebExchange.class);

        GatewayFilterChain chainMock = mock(GatewayFilterChain.class);
        given(chainMock.filter(any())).willReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchangeMock, chainMock))
                .expectComplete()
                .verify();

        then(chainMock).should().filter(exchangeCaptor.capture());

        List<String> actualParamValues = exchangeCaptor.getValue()
                .getRequest()
                .getQueryParams()
                .get(paramInitializer.getParamName());
        List<String> expectedParamValues = testParamValues.stream()
                .map(String::valueOf)
                .toList();
        assertThat(actualParamValues)
                .asList()
                .containsExactlyInAnyOrderElementsOf(expectedParamValues);
    }
}