package com.example.dynamicgateway.config.security.filter;

import com.example.dynamicgateway.service.authenticationExtractor.AuthenticationExtractor;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpResponse;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.springframework.web.server.WebHandler;
import org.springframework.web.server.handler.DefaultWebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class AuthenticationExtractionWebFilterTest {
    @InjectMocks
    private AuthenticationExtractionWebFilter filter;
    @Mock
    private AuthenticationExtractor authenticationExtractorMock;
    @Mock
    private WebFilterChain chainMock;

    @Test
    void testFilter_withNoAuthentication_exchangeFilteredFurther() {
        ServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/"));
        given(authenticationExtractorMock.isSupportedSource(exchange)).willReturn(false);
        given(chainMock.filter(exchange)).willReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, chainMock))
                .expectComplete()
                .verify();

        then(chainMock).should().filter(exchange);
    }

    @Test
    void testFilter_withValidToken_exchangeFilteredFurther() {
        TestingAuthenticationToken token = new TestingAuthenticationToken("jack", "12345",
                List.of(new SimpleGrantedAuthority("user")));
        ServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/"));

        given(authenticationExtractorMock.isSupportedSource(exchange)).willReturn(true);
        given(authenticationExtractorMock.tryExtractAuthentication(exchange)).willReturn(Mono.just(token));

        given(chainMock.filter(exchange)).willReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, chainMock))
                .expectComplete()
                .verify();

        then(chainMock).should().filter(exchange);
    }

    @Test
    void testFilter_withValidToken_authenticationPassedToSecurityContextHolder() {
        String sub = "mickey_m";
        List<String> roles = List.of("user", "admin");
        TestingAuthenticationToken authenticationToken = new TestingAuthenticationToken(
                sub, null, roles.stream().map(SimpleGrantedAuthority::new).toList()
        );

        MockServerHttpRequest request = MockServerHttpRequest
                .get("/")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        given(authenticationExtractorMock.isSupportedSource(exchange)).willReturn(true);
        given(authenticationExtractorMock.tryExtractAuthentication(exchange)).willReturn(Mono.just(authenticationToken));

        WebHandler handlerMock = mock(WebHandler.class);
        given(handlerMock.handle(exchange)).willReturn(Mono.empty());

        WebFilter authenticationAssertingFilter = (e, c) -> ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .switchIfEmpty(Mono.error(new AssertionError("No authentication")))
                .filter(a -> isExpectedAuthentication(a, sub, roles))
                .switchIfEmpty(Mono.error(new AssertionError("Authentication doesn't match expected parameters")))
                .flatMap(a -> c.filter(e));

        WebFilterChain webFilterChain = new DefaultWebFilterChain(handlerMock, List.of(
                filter, authenticationAssertingFilter));

        StepVerifier.create(webFilterChain.filter(exchange))
                .verifyComplete();
    }

    private boolean isExpectedAuthentication(Authentication authentication, Object principal, List<String> roleStrings) {
        List<String> roleStringsFromAuthentication = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        return authentication.getPrincipal().equals(principal) &&
                roleStrings.containsAll(roleStringsFromAuthentication) &&
                roleStringsFromAuthentication.containsAll(roleStrings);
    }

    @Test
    void testFilter_ifBadCredentialsThrown_doesntFilter_returns401responseWithNonBlankBody() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        given(authenticationExtractorMock.isSupportedSource(exchange)).willReturn(true);
        given(authenticationExtractorMock.tryExtractAuthentication(exchange)).willThrow(new BadCredentialsException("Invalid token"));

        StepVerifier.create(filter.filter(exchange, chainMock))
                .expectComplete()
                .verify();

        then(chainMock).should(never()).filter(exchange);

        ServerHttpResponse response = exchange.getResponse();
        assertThat(response).extracting(ServerHttpResponse::getStatusCode).isEqualTo(HttpStatus.UNAUTHORIZED);

        StepVerifier.create(((MockServerHttpResponse) response).getBodyAsString())
                .expectNextMatches(StringUtils::isNotBlank)
                .expectComplete()
                .verify();
    }
}
