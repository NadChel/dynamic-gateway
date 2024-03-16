package com.example.dynamicgateway.service.routeProcessor;

import com.example.dynamicgateway.config.RouteAssemblerConfig;
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
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.will;
import static org.mockito.BDDMockito.willReturn;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class RouteAssemblerTest {
    private final Route.AsyncBuilder routeBuilder = Route.async();
    @Mock
    private GatewayMeta gatewayMetaMock;
    @InjectMocks
    private RouteAssemblerConfig routeAssemblerConfig;

    @Test
    void pathPredicateRouteAssembler_addsPredicate_thatMatchesMatchingRequests() {
        given(gatewayMetaMock.getVersionPrefix()).willReturn("/api/v1");
        given(gatewayMetaMock.getIgnoredPrefixes()).willReturn(List.of("/auth"));

        DocumentedEndpoint<?> endpoint = SwaggerEndpointStub.builder()
                .method(HttpMethod.GET)
                .path("/auth/test-path")
                .build();

        EndpointRouteAssembler basePredicateRouteProcessor = routeAssemblerConfig.pathPredicateRouteAssembler();
        basePredicateRouteProcessor.process(routeBuilder, endpoint);

        MockServerWebExchange exchangeMock = MockServerWebExchange.builder(
                MockServerHttpRequest.method(HttpMethod.GET, "/api/v1/test-path")
        ).build();

        StepVerifier.create(routeBuilder.getPredicate().apply(exchangeMock))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void pathPredicateRouteAssembler_addsPredicate_thatDoesntMatchNonMatchingRequests() {
        given(gatewayMetaMock.getVersionPrefix()).willReturn("/api/v1");

        DocumentedEndpoint<?> endpoint = SwaggerEndpointStub.builder()
                .method(HttpMethod.GET)
                .path("/auth/test-path")
                .build();

        EndpointRouteAssembler basePredicateRouteProcessor = routeAssemblerConfig.pathPredicateRouteAssembler();
        basePredicateRouteProcessor.process(routeBuilder, endpoint);

        MockServerWebExchange nonMatchingExchangeMock = MockServerWebExchange.builder(
                MockServerHttpRequest.method(HttpMethod.GET, "/auth/test-path")
        ).build();

        StepVerifier.create(routeBuilder.getPredicate().apply(nonMatchingExchangeMock))
                .expectNext(false)
                .verifyComplete();

        MockServerWebExchange anotherNonMatchingExchangeMock = MockServerWebExchange.builder(
                MockServerHttpRequest.method(HttpMethod.GET, "/api/v1")
        ).build();

        StepVerifier.create(routeBuilder.getPredicate().apply(anotherNonMatchingExchangeMock))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void versionPrefixRemovingRouteAssembler_addsFilter_thatRemovesVersionPrefix() {
        String prefix = "/api/v1";
        given(gatewayMetaMock.getVersionPrefix()).willReturn(prefix);

        EndpointRouteAssembler versionPrefixRemovingRouteAssembler =
                routeAssemblerConfig.versionPrefixRemovingRouteAssembler();
        versionPrefixRemovingRouteAssembler.process(routeBuilder, null);

        List<GatewayFilter> filters = RouteBuilderUtil.getFilters(routeBuilder);

        assumeThat(filters.size()).isEqualTo(1);

        ServerWebExchange exchangeMock = MockServerWebExchange.from(
                MockServerHttpRequest.method(HttpMethod.POST, "/api/v1/some-path")
        );

        assumeThat(exchangeMock).extracting(ServerWebExchange::getRequest)
                .extracting(ServerHttpRequest::getPath)
                .extracting(PathContainer::value)
                .asString()
                .startsWith(prefix);

        ArgumentCaptor<ServerWebExchange> exchangeCaptor = ArgumentCaptor.forClass(ServerWebExchange.class);

        GatewayFilterChain chainMock = mock(GatewayFilterChain.class);
        given(chainMock.filter(any())).willReturn(Mono.empty());

        GatewayFilter versionPrefixRemovingFilter = filters.get(0);
        StepVerifier.create(versionPrefixRemovingFilter.filter(exchangeMock, chainMock))
                .verifyComplete();

        then(chainMock).should().filter(exchangeCaptor.capture());

        String requestPath = exchangeCaptor.getValue()
                .getRequest()
                .getPath()
                .value();
        assertThat(requestPath).asString().doesNotStartWith(prefix);
    }

    @Test
    void uriRouteAssembler_setsUriToSchemePlusAppName() {
        String scheme = "test://";
        String appName = "test-app";

        DiscoverableApplication<?> discoverableAppMock = mock(DiscoverableApplication.class);
        given(discoverableAppMock.getDiscoveryServiceScheme()).willReturn(scheme);
        given(discoverableAppMock.getName()).willReturn(appName);

        DocumentedEndpoint<?> endpointMock = mock(DocumentedEndpoint.class, RETURNS_DEEP_STUBS);
        given(endpointMock.getDeclaringApp().getDiscoverableApp()).will(i -> discoverableAppMock);

        EndpointRouteAssembler uriRouteProcessor = routeAssemblerConfig.uriRouteAssembler();
        uriRouteProcessor.process(routeBuilder, endpointMock);

        assertThat(RouteBuilderUtil.getUri(routeBuilder)).isEqualTo(URI.create(scheme + appName));
    }

    @Test
    void ignoredPrefixAppendingRouteAssembler_addsFilter_thatAppendsIgnoredPrefix() {
        String prefix = "/some-prefix";
        String path = "/some-path";
        String prefixedPath = prefix + path;

        DocumentedEndpoint<?> endpointMock = mock(DocumentedEndpoint.class, RETURNS_DEEP_STUBS);
        given(endpointMock.getDetails().getPath()).willReturn(prefixedPath);

        given(gatewayMetaMock.getIgnoredPrefixes()).willReturn(List.of(prefix));

        EndpointRouteAssembler appendEndpointPrefixRouteProcessor =
                routeAssemblerConfig.ignoredPrefixAppendingRouteAssembler();
        appendEndpointPrefixRouteProcessor.process(routeBuilder, endpointMock);

        List<GatewayFilter> filters = RouteBuilderUtil.getFilters(routeBuilder);
        assumeThat(filters.size()).isEqualTo(1);

        ServerWebExchange exchangeMock = MockServerWebExchange.from(
                MockServerHttpRequest.method(HttpMethod.DELETE, path)
        );
        assumeThat(exchangeMock).extracting(ServerWebExchange::getRequest)
                .extracting(ServerHttpRequest::getPath)
                .extracting(PathContainer::value)
                .asString()
                .doesNotStartWith(prefix);

        ArgumentCaptor<ServerWebExchange> exchangeCaptor = ArgumentCaptor.forClass(ServerWebExchange.class);

        GatewayFilterChain chainMock = mock(GatewayFilterChain.class);
        given(chainMock.filter(any())).willReturn(Mono.empty());

        GatewayFilter ignoredPrefixAppendingFilter = filters.get(0);
        StepVerifier.create(ignoredPrefixAppendingFilter.filter(exchangeMock, chainMock))
                .verifyComplete();

        then(chainMock).should().filter(exchangeCaptor.capture());

        String requestPath = exchangeCaptor.getValue()
                .getRequest()
                .getPath()
                .value();
        assertThat(requestPath).asString().startsWith(prefix);
    }

    @Test
    void idRouteAssembler_setsId() {
        assumeThat(routeBuilder.getId()).isNull();

        EndpointRouteAssembler idRouteProcessor = routeAssemblerConfig.idRouteAssembler();
        idRouteProcessor.process(routeBuilder, null);

        assertThat(routeBuilder.getId()).isNotNull();
    }

    @Test
    void methodRouteAssembler_addsPredicate_matchingEndpointsMethod() {
        HttpMethod matchingMethod = HttpMethod.PATCH;
        DocumentedEndpoint<?> endpointMock = mock(DocumentedEndpoint.class, RETURNS_DEEP_STUBS);
        given(endpointMock.getDetails().getMethod()).willReturn(matchingMethod);

        EndpointRouteAssembler methodRouteProcessor = routeAssemblerConfig.methodRouteAssembler();
        methodRouteProcessor.process(routeBuilder, endpointMock);

        MockServerWebExchange matchingExchangeMock = MockServerWebExchange.builder(
                MockServerHttpRequest.method(matchingMethod, "/")
        ).build();

        AsyncPredicate<ServerWebExchange> routePredicate = routeBuilder.getPredicate();

        StepVerifier.create(routePredicate.apply(matchingExchangeMock))
                .expectNext(true)
                .verifyComplete();

        MockServerWebExchange nonMatchingExchangeMock = MockServerWebExchange.builder(
                MockServerHttpRequest.method(HttpMethod.HEAD, "/")
        ).build();

        StepVerifier.create(routePredicate.apply(nonMatchingExchangeMock))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void paramInitializingRouteAssembler_doesntDoAnything_ifNoParams() {
        DocumentedEndpoint<?> endpointMock = mock(DocumentedEndpoint.class, RETURNS_DEEP_STUBS);
        given(endpointMock.getDetails().getParameters()).willReturn(Collections.emptyList());

        ParamInitializer paramInitializerMock = mock(ParamInitializer.class);
        given(paramInitializerMock.getParamName()).willReturn("someParam");

        assumeThat(RouteBuilderUtil.getFilters(routeBuilder)).isEmpty();

        EndpointRouteAssembler paramInitializingRouteProcessor =
                routeAssemblerConfig.paramInitializingRouteAssembler(
                        new ParamInitializers(List.of(paramInitializerMock)));
        paramInitializingRouteProcessor.process(routeBuilder, endpointMock);

        assertThat(RouteBuilderUtil.getFilters(routeBuilder)).isEmpty();
    }

    @Test
    void paramInitializingRouteAssembler_doesntDoAnything_ifNoParamInitializersPassed() {
        List<SwaggerParameter> params = Stream.of("paramOne", "paramTwo")
                .map(SwaggerParameter::new)
                .toList();

        EndpointDetails detailsMock = mock(EndpointDetails.class);
        willReturn(params).given(detailsMock).getParameters();

        DocumentedEndpoint<?> endpointMock = mock(DocumentedEndpoint.class);
        given(endpointMock.getDetails()).willReturn(detailsMock);

        assumeThat(RouteBuilderUtil.getFilters(routeBuilder)).isEmpty();

        EndpointRouteAssembler paramInitializingRouteAssembler =
                routeAssemblerConfig.paramInitializingRouteAssembler(new ParamInitializers(Collections.emptyList()));
        paramInitializingRouteAssembler.process(routeBuilder, endpointMock);

        assertThat(RouteBuilderUtil.getFilters(routeBuilder)).isEmpty();
    }

    @Test
    void paramInitializingRouteProcessor_withParamInitializerPassed_withMatchingParam_hasParamInitializerPassFilter() {
        EndpointParameter param = mock(EndpointParameter.class);

        EndpointDetails detailsMock = mock(EndpointDetails.class);
        willReturn(List.of(param)).given(detailsMock).getParameters();

        DocumentedEndpoint<?> endpointMock = mock(DocumentedEndpoint.class);
        given(endpointMock.getDetails()).willReturn(detailsMock);

        GatewayFilter filterMock = mock(GatewayFilter.class);

        ParamInitializer paramInitializerMock = mock(ParamInitializer.class);
        will(
                invocation -> invocation.getArgument(0, Route.AsyncBuilder.class).filter(filterMock)
        ).given(paramInitializerMock).addInitializingFilter(routeBuilder);

        ParamInitializers paramInitializersMock = mock(ParamInitializers.class);
        given(paramInitializersMock.findInitializerForParam(param))
                .willReturn(Optional.of(paramInitializerMock));

        EndpointRouteAssembler paramInitializingRouteAssembler =
                routeAssemblerConfig.paramInitializingRouteAssembler(paramInitializersMock);

        List<GatewayFilter> filters = RouteBuilderUtil.getFilters(routeBuilder);
        assumeThat(filters).isEmpty();

        paramInitializingRouteAssembler.process(routeBuilder, endpointMock);

        assertThat(filters).hasSize(1);
        assertThat(filters.get(0)).isEqualTo(filterMock);
    }
}