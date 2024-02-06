package com.example.dynamicgateway.service.endpointSieve;

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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
        when(gatewayMetaMock.getIgnoredPatterns()).thenReturn(Collections.emptyList());

        errorPathEndpointSieve = endpointSieveConfig.errorPathEndpointSieve(gatewayMetaMock, antPathMatcherMock);
        for (int i = 0; i < 20; i++) {
            UUID uuid = UUID.randomUUID();
            SwaggerEndpoint endpointToKeep = SwaggerEndpointStub.builder().path("/" + uuid).build();
            assertThat(errorPathEndpointSieve.isAllowed(endpointToKeep)).isTrue();
        }
    }

    @Test
    void doesntAllowIgnoredPatterns() {
        String ignoredPattern = "/ignored-path/**";
        String anotherIgnoredPattern = "/*/another-ignored-path";
        when(gatewayMetaMock.getIgnoredPatterns()).thenReturn(List.of(
                ignoredPattern, anotherIgnoredPattern
        ));

        String pathToExclude = "/ignored-path";
        String anotherPathToExclude = "/it-is/another-ignored-path";

        when(antPathMatcherMock.match(any(), any())).thenReturn(false);
        when(antPathMatcherMock.match(ignoredPattern, pathToExclude)).thenReturn(true);
        when(antPathMatcherMock.match(anotherIgnoredPattern, anotherPathToExclude)).thenReturn(true);

        DocumentedEndpoint<?> endpointToExclude = mock(DocumentedEndpoint.class, RETURNS_DEEP_STUBS);
        when(endpointToExclude.getDetails().getPath()).thenReturn(pathToExclude);

        DocumentedEndpoint<?> anotherEndpointToExclude = mock(DocumentedEndpoint.class, RETURNS_DEEP_STUBS);
        when(anotherEndpointToExclude.getDetails().getPath()).thenReturn(anotherPathToExclude);

        String okPath = "/another-ignored-path/on-second-thought-it-is-not";
        SwaggerEndpoint endpointToKeep = mock(SwaggerEndpoint.class, RETURNS_DEEP_STUBS);
        when(endpointToKeep.getDetails().getPath()).thenReturn(okPath);

        errorPathEndpointSieve = endpointSieveConfig.errorPathEndpointSieve(gatewayMetaMock, antPathMatcherMock);

        assertThat(errorPathEndpointSieve.isAllowed(endpointToExclude)).isFalse();
        assertThat(errorPathEndpointSieve.isAllowed(anotherEndpointToExclude)).isFalse();

        assertThat(errorPathEndpointSieve.isAllowed(endpointToKeep)).isTrue();
    }
}