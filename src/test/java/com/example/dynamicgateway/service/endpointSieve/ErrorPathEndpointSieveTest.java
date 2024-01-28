package com.example.dynamicgateway.service.endpointSieve;

import com.example.dynamicgateway.config.EndpointSieveConfig;
import com.example.dynamicgateway.model.documentedEndpoint.DocumentedEndpoint;
import com.example.dynamicgateway.model.gatewayMeta.GatewayMeta;
import org.junit.jupiter.api.Test;
import org.springframework.util.AntPathMatcher;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ErrorPathEndpointSieveTest {
    @Test
    void allowsAnything_ifNoIgnoredPatternsProvided() {
        GatewayMeta gatewayMetaMock = mock(GatewayMeta.class);
        when(gatewayMetaMock.ignoredPatterns()).thenReturn(new String[0]);

        AntPathMatcher antPathMatcherMock = mock(AntPathMatcher.class);

        EndpointSieve errorPathEndpointSieve = new EndpointSieveConfig().errorPathEndpointSieve(gatewayMetaMock, antPathMatcherMock);

        for (int i = 0; i < 20; i++) {
            UUID uuid = UUID.randomUUID();
            DocumentedEndpoint<?> endpointToKeep = mock(DocumentedEndpoint.class, RETURNS_DEEP_STUBS);
            when(endpointToKeep.getDetails().getPath()).thenReturn(String.valueOf(uuid));
            assertThat(errorPathEndpointSieve.isAllowed(endpointToKeep)).isTrue();
        }

    }

    @Test
    void doesntAllowIgnoredPatterns() {
        GatewayMeta gatewayMetaMock = mock(GatewayMeta.class);
        String ignoredPattern = "/ignored-path/**";
        String anotherIgnoredPattern = "/*/another-ignored-path";
        when(gatewayMetaMock.ignoredPatterns()).thenReturn(new String[]{
                ignoredPattern, anotherIgnoredPattern
        });

        String pathToExclude = "/ignored-path";
        String anotherPathToExclude = "/it-is/another-ignored-path";
        AntPathMatcher antPathMatcherMock = mock(AntPathMatcher.class);
        when(antPathMatcherMock.match(ignoredPattern, pathToExclude)).thenReturn(true);
        when(antPathMatcherMock.match(anotherIgnoredPattern, anotherPathToExclude)).thenReturn(true);

        DocumentedEndpoint<?> endpointToExclude = mock(DocumentedEndpoint.class, RETURNS_DEEP_STUBS);
        when(endpointToExclude.getDetails().getPath()).thenReturn(pathToExclude);

        DocumentedEndpoint<?> anotherEndpointToExclude = mock(DocumentedEndpoint.class, RETURNS_DEEP_STUBS);
        when(anotherEndpointToExclude.getDetails().getPath()).thenReturn(anotherPathToExclude);

        String okPath = "/another-ignored-path/on-second-thought-it-is-not";
        DocumentedEndpoint<?> endpointToKeep = mock(DocumentedEndpoint.class, RETURNS_DEEP_STUBS);
        when(endpointToKeep.getDetails().getPath()).thenReturn(okPath);

        EndpointSieve errorPathEndpointSieve = new EndpointSieveConfig().errorPathEndpointSieve(gatewayMetaMock, antPathMatcherMock);

        assertThat(errorPathEndpointSieve.isAllowed(endpointToExclude)).isFalse();
        assertThat(errorPathEndpointSieve.isAllowed(anotherEndpointToExclude)).isFalse();
        assertThat(errorPathEndpointSieve.isAllowed(endpointToKeep)).isTrue();
    }
}