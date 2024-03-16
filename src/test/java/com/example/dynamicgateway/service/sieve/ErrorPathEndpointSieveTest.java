package com.example.dynamicgateway.service.sieve;

import com.example.dynamicgateway.config.EndpointSieveConfig;
import com.example.dynamicgateway.model.documentedEndpoint.DocumentedEndpoint;
import com.example.dynamicgateway.model.documentedEndpoint.SwaggerEndpoint;
import com.example.dynamicgateway.model.gatewayMeta.GatewayMeta;
import com.example.dynamicgateway.testModel.SwaggerEndpointStub;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.AntPathMatcher;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class ErrorPathEndpointSieveTest {
    private final EndpointSieveConfig endpointSieveConfig = new EndpointSieveConfig();
    @Mock
    private GatewayMeta gatewayMetaMock;
    @Mock
    private AntPathMatcher antPathMatcherMock;
    private EndpointSieve errorPathEndpointSieve;

    @Test
    void allowsAnything_ifNoIgnoredPatternsProvided() {
        given(gatewayMetaMock.getIgnoredPatterns()).willReturn(Collections.emptyList());

        errorPathEndpointSieve = endpointSieveConfig.errorPathEndpointSieve(gatewayMetaMock, antPathMatcherMock);
        int reasonableNumberOfRandomEndpoints = 20;
        for (int i = 0; i < reasonableNumberOfRandomEndpoints; i++) {
            UUID uuid = UUID.randomUUID();
            SwaggerEndpoint endpointToKeep = SwaggerEndpointStub.builder().path("/" + uuid).build();
            assertThat(errorPathEndpointSieve.isAllowed(endpointToKeep)).isTrue();
        }
    }

    @Test
    void doesntAllowIgnoredPatterns() {
        String ignoredPattern = "/ignored-path/**";
        String anotherIgnoredPattern = "/*/another-ignored-path";
        given(gatewayMetaMock.getIgnoredPatterns()).willReturn(List.of(
                ignoredPattern, anotherIgnoredPattern
        ));

        String pathToExclude = "/ignored-path";
        String anotherPathToExclude = "/it-is/another-ignored-path";

        given(antPathMatcherMock.match(any(), any())).willReturn(false);
        given(antPathMatcherMock.match(ignoredPattern, pathToExclude)).willReturn(true);
        given(antPathMatcherMock.match(anotherIgnoredPattern, anotherPathToExclude)).willReturn(true);

        DocumentedEndpoint<?> endpointToExclude = mock(DocumentedEndpoint.class, RETURNS_DEEP_STUBS);
        given(endpointToExclude.getDetails().getPath()).willReturn(pathToExclude);

        DocumentedEndpoint<?> anotherEndpointToExclude = mock(DocumentedEndpoint.class, RETURNS_DEEP_STUBS);
        given(anotherEndpointToExclude.getDetails().getPath()).willReturn(anotherPathToExclude);

        String okPath = "/another-ignored-path/on-second-thought-it-is-not";
        SwaggerEndpoint endpointToKeep = mock(SwaggerEndpoint.class, RETURNS_DEEP_STUBS);
        given(endpointToKeep.getDetails().getPath()).willReturn(okPath);

        errorPathEndpointSieve = endpointSieveConfig.errorPathEndpointSieve(gatewayMetaMock, antPathMatcherMock);

        assertThat(errorPathEndpointSieve.isAllowed(endpointToExclude)).isFalse();
        assertThat(errorPathEndpointSieve.isAllowed(anotherEndpointToExclude)).isFalse();

        assertThat(errorPathEndpointSieve.isAllowed(endpointToKeep)).isTrue();
    }
}