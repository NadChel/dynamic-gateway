package com.example.dynamicgateway.service.routeProcessor;

import com.example.dynamicgateway.config.RouteProcessorConfig;
import com.example.dynamicgateway.model.discoverableApplication.DiscoverableApplication;
import com.example.dynamicgateway.model.documentedEndpoint.DocumentedEndpoint;
import com.example.dynamicgateway.model.endpointDetails.EndpointDetails;
import com.example.dynamicgateway.model.endpointParameter.EndpointParameter;
import com.example.dynamicgateway.model.endpointParameter.SwaggerParameter;
import com.example.dynamicgateway.model.gatewayMeta.GatewayMeta;
import com.example.dynamicgateway.service.paramInitializer.ParamInitializer;
import com.example.dynamicgateway.service.paramInitializer.ParamInitializers;
import com.example.dynamicgateway.testModel.SwaggerEndpointStub;
import com.example.dynamicgateway.testUtil.RouteBuilderUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.handler.AsyncPredicate;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.PathContainer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RouteProcessorTest {
    private final Route.AsyncBuilder testRoute = Route.async();
    @Mock
    private GatewayMeta gatewayMetaMock;
    @InjectMocks
    private RouteProcessorConfig routeProcessorConfig;

    @Test
    void basePredicateProcessor_withMatchingRequest() {
        when(gatewayMetaMock.versionPrefix()).thenReturn("/api/v1");

        DocumentedEndpoint<?> testEndpoint = SwaggerEndpointStub.builder()
                .method(HttpMethod.GET).path("/auth/test-path").build();

        EndpointRouteProcessor basePredicateRouteProcessor = routeProcessorConfig.basePredicateProcessor();
        basePredicateRouteProcessor.process(testRoute, testEndpoint);

        MockServerWebExchange exchangeMock = MockServerWebExchange.builder(
                MockServerHttpRequest.method(HttpMethod.GET, "/api/v1/test-path")
        ).build();

        StepVerifier.create(testRoute.getPredicate().apply(exchangeMock))
                .expectNext(true)
                .expectComplete()
                .verify();
    }

    @Test
    void basePredicateProcessor_withNonMatchingRequest() {
        when(gatewayMetaMock.versionPrefix()).thenReturn("/api/v1");

        DocumentedEndpoint<?> testEndpoint = SwaggerEndpointStub.builder()
                .method(HttpMethod.GET).path("/auth/test-path").build();

        EndpointRouteProcessor basePredicateRouteProcessor = routeProcessorConfig.basePredicateProcessor();
        basePredicateRouteProcessor.process(testRoute, testEndpoint);

        MockServerWebExchange nonMatchingExchangeMock = MockServerWebExchange.builder(
                MockServerHttpRequest.method(HttpMethod.GET, "/auth/test-path")
        ).build();

        StepVerifier.create(testRoute.getPredicate().apply(nonMatchingExchangeMock))
                .expectNext(false)
                .expectComplete()
                .verify();

        MockServerWebExchange anotherNonMatchingExchangeMock = MockServerWebExchange.builder(
                MockServerHttpRequest.method(HttpMethod.GET, "/api/v1")
        ).build();

        StepVerifier.create(testRoute.getPredicate().apply(anotherNonMatchingExchangeMock))
                .expectNext(false)
                .expectComplete()
                .verify();
    }

    @Test
    void removeGatewayPrefixRouteProcessor() {
        String testGatewayPrefix = "/api/v1";
        when(gatewayMetaMock.versionPrefix()).thenReturn(testGatewayPrefix);

        EndpointRouteProcessor removeGatewayPrefixRouteProcessor = routeProcessorConfig.removeGatewayPrefixRouteProcessor();
        removeGatewayPrefixRouteProcessor.process(testRoute, null);

        List<GatewayFilter> filters = RouteBuilderUtil.getFilters(testRoute);

        assumeThat(filters.size()).isEqualTo(1);

        ServerWebExchange exchangeMock = MockServerWebExchange.from(
                MockServerHttpRequest.method(HttpMethod.POST, "/api/v1/some-path")
        );

        assumeThat(exchangeMock).extracting(ServerWebExchange::getRequest)
                .extracting(ServerHttpRequest::getPath)
                .extracting(PathContainer::value)
                .asString().startsWith(testGatewayPrefix);

        ArgumentCaptor<ServerWebExchange> exchangeCaptor = ArgumentCaptor.forClass(ServerWebExchange.class);

        GatewayFilterChain chainMock = mock(GatewayFilterChain.class);
        when(chainMock.filter(any())).thenReturn(Mono.empty());

        StepVerifier.create(filters.get(0).filter(exchangeMock, chainMock))
                .expectComplete()
                .verify();

        verify(chainMock).filter(exchangeCaptor.capture());

        String requestPath = exchangeCaptor.getValue().getRequest().getPath().value();

        assertThat(requestPath).asString().doesNotStartWith(testGatewayPrefix);
    }

    @Test
    void uriRouteProcessor() {
        String testScheme = "test://";
        String testAppName = "test-app";

        DiscoverableApplication discoverableAppMock = mock(DiscoverableApplication.class);
        when(discoverableAppMock.getDiscoveryServiceScheme()).thenReturn(testScheme);
        when(discoverableAppMock.getName()).thenReturn(testAppName);

        DocumentedEndpoint<?> endpointMock = mock(DocumentedEndpoint.class, RETURNS_DEEP_STUBS);
        when(endpointMock.getDeclaringApp().getDiscoverableApp()).thenReturn(discoverableAppMock);

        EndpointRouteProcessor uriRouteProcessor = routeProcessorConfig.uriRouteProcessor();
        uriRouteProcessor.process(testRoute, endpointMock);

        assertThat(RouteBuilderUtil.getUri(testRoute)).isEqualTo(URI.create(testScheme + testAppName));
    }

    @Test
    void appendEndpointPrefixRouteProcessor() {
        String testPrefix = "/some-test-prefix";

        DocumentedEndpoint<?> endpointMock = mock(DocumentedEndpoint.class, RETURNS_DEEP_STUBS);
        when(endpointMock.getDetails().getPrefix()).thenReturn(testPrefix);

        EndpointRouteProcessor appendEndpointPrefixRouteProcessor = routeProcessorConfig.appendEndpointPrefixRouteProcessor();
        appendEndpointPrefixRouteProcessor.process(testRoute, endpointMock);

        List<GatewayFilter> filters = RouteBuilderUtil.getFilters(testRoute);

        assumeThat(filters.size()).isEqualTo(1);

        String testPath = "/some-path";
        ServerWebExchange exchangeMock = MockServerWebExchange.from(
                MockServerHttpRequest.method(HttpMethod.DELETE, testPath)
        );

        assumeThat(exchangeMock).extracting(ServerWebExchange::getRequest)
                .extracting(ServerHttpRequest::getPath)
                .extracting(PathContainer::value)
                .asString().doesNotStartWith(testPrefix);

        ArgumentCaptor<ServerWebExchange> exchangeCaptor = ArgumentCaptor.forClass(ServerWebExchange.class);

        GatewayFilterChain chainMock = mock(GatewayFilterChain.class);
        when(chainMock.filter(any())).thenReturn(Mono.empty());

        StepVerifier.create(filters.get(0).filter(exchangeMock, chainMock))
                .expectComplete()
                .verify();

        verify(chainMock).filter(exchangeCaptor.capture());

        String requestPath = exchangeCaptor.getValue().getRequest().getPath().value();

        assertThat(requestPath).asString().startsWith(testPrefix);
    }

    @Test
    void idRouteProcessor() {
        assumeThat(testRoute.getId()).isNull();

        EndpointRouteProcessor idRouteProcessor = routeProcessorConfig.idRouteProcessor();
        idRouteProcessor.process(testRoute, null);

        assertThat(testRoute.getId()).isNotNull();
    }

    @Test
    void methodRouteProcessor() {
        DocumentedEndpoint<?> endpointMock = mock(DocumentedEndpoint.class, RETURNS_DEEP_STUBS);
        when(endpointMock.getDetails().getMethod()).thenReturn(HttpMethod.PATCH);

        EndpointRouteProcessor methodRouteProcessor = routeProcessorConfig.methodRouteProcessor();
        methodRouteProcessor.process(testRoute, endpointMock);

        MockServerWebExchange matchingExchangeMock = MockServerWebExchange.builder(
                MockServerHttpRequest.method(HttpMethod.PATCH, "/")
        ).build();

        AsyncPredicate<ServerWebExchange> routePredicate = testRoute.getPredicate();

        StepVerifier.create(routePredicate.apply(matchingExchangeMock))
                .expectNext(true)
                .expectComplete()
                .verify();

        MockServerWebExchange nonMatchingExchangeMock = MockServerWebExchange.builder(
                MockServerHttpRequest.method(HttpMethod.HEAD, "/")
        ).build();

        StepVerifier.create(routePredicate.apply(nonMatchingExchangeMock))
                .expectNext(false)
                .expectComplete()
                .verify();
    }

    @Test
    @SuppressWarnings("ReactiveStreamsUnusedPublisher")
    void paramInitializingRouteProcessor_doesntDoAnything_ifNoParams() {
        DocumentedEndpoint<?> endpointMock = mock(DocumentedEndpoint.class, RETURNS_DEEP_STUBS);
        when(endpointMock.getDetails().getParameters()).thenReturn(Collections.emptyList());

        ParamInitializer paramInitializerMock = mock(ParamInitializer.class);
        when(paramInitializerMock.getParamName()).thenReturn("someParam");
        lenient().doReturn(Flux.just(1, 2, 3)).when(paramInitializerMock).getParamValues(any());

        EndpointRouteProcessor paramInitializingRouteProcessor =
                routeProcessorConfig.paramInitializingRouteProcessor(new ParamInitializers(List.of(
                        paramInitializerMock
                )));
        paramInitializingRouteProcessor.process(testRoute, endpointMock);

        assertThat(RouteBuilderUtil.getFilters(testRoute)).asList().isEmpty();
    }

    @Test
    void paramInitializingRouteProcessor_doesntDoAnything_ifNoParamInitializersPassed() {
        List<SwaggerParameter> testParams = Stream.of("paramOne", "paramTwo").map(SwaggerParameter::new).toList();

        EndpointDetails detailsMock = mock(EndpointDetails.class);
        doReturn(testParams).when(detailsMock).getParameters();

        DocumentedEndpoint<?> endpointMock = mock(DocumentedEndpoint.class);
        when(endpointMock.getDetails()).thenReturn(detailsMock);

        EndpointRouteProcessor paramInitializingRouteProcessor =
                routeProcessorConfig.paramInitializingRouteProcessor(new ParamInitializers(Collections.emptyList()));
        paramInitializingRouteProcessor.process(testRoute, endpointMock);

        assertThat(RouteBuilderUtil.getFilters(testRoute)).asList().isEmpty();
    }

    @Test
    void paramInitializingRouteProcessor_withParamInitializerPassed_withMatchingParam_hasParamInitializerPassFilter() {
        EndpointParameter someParam = mock(EndpointParameter.class);

        EndpointDetails detailsMock = mock(EndpointDetails.class);
        doReturn(List.of(someParam)).when(detailsMock).getParameters();

        DocumentedEndpoint<?> endpointMock = mock(DocumentedEndpoint.class);
        when(endpointMock.getDetails()).thenReturn(detailsMock);

        GatewayFilter filterMock = mock(GatewayFilter.class);

        ParamInitializer paramInitializerMock = mock(ParamInitializer.class);
        doAnswer(
                invocation -> invocation.getArgument(0, Route.AsyncBuilder.class).filter(filterMock)
        ).when(paramInitializerMock).initialize(testRoute);

        ParamInitializers paramInitializersMock = mock(ParamInitializers.class);
        when(paramInitializersMock.findInitializerForParam(someParam))
                .thenReturn(Optional.of(paramInitializerMock));

        EndpointRouteProcessor paramInitializingRouteProcessor =
                routeProcessorConfig.paramInitializingRouteProcessor(paramInitializersMock);

        List<GatewayFilter> filters = RouteBuilderUtil.getFilters(testRoute);

        assumeThat(filters).asList().isEmpty();

        paramInitializingRouteProcessor.process(testRoute, endpointMock);

        assertThat(filters).asList().hasSize(1);
        assertThat(filters.get(0)).isEqualTo(filterMock);
    }
}