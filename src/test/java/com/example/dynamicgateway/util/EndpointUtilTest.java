package com.example.dynamicgateway.util;

import com.example.dynamicgateway.model.documentedEndpoint.DocumentedEndpoint;
import com.example.dynamicgateway.model.gatewayMeta.GatewayMeta;
import com.example.dynamicgateway.testModel.SwaggerEndpointStub;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class EndpointUtilTest {
    @Mock
    GatewayMeta gatewayMetaMock;

    @Test
    void pathWithRemovedPrefix_returnsUnchangedPath_ifNoIgnoredPrefixesProvided() {
        String path = "/auth/test-path";
        DocumentedEndpoint<?> endpoint = SwaggerEndpointStub.builder().path(path).build();

        given(gatewayMetaMock.getIgnoredPrefixes()).willReturn(Collections.emptyList());

        String returnedPath = EndpointUtil.pathWithRemovedPrefix(endpoint, gatewayMetaMock);
        assertThat(returnedPath).isEqualTo(path);
    }

    @Test
    void pathWithRemovedPrefix_returnsPathWithoutPrefix_ifStartsWithIgnoredPrefix() {
        String prefix = "/auth";
        String path = prefix + "/test-path";
        DocumentedEndpoint<?> endpoint = SwaggerEndpointStub.builder().path(path).build();

        given(gatewayMetaMock.getIgnoredPrefixes()).willReturn(List.of(prefix));

        String returnedPath = EndpointUtil.pathWithRemovedPrefix(endpoint, gatewayMetaMock);

        String expectedPath = path.substring(prefix.length());
        assertThat(returnedPath).isEqualTo(expectedPath);
    }

    @Test
    void pathWithRemovedPrefix_ifMultiplePrefixesMatch_returnsPathWithoutLongestIgnoredPrefix() {
        String prefix = "/auth";
        String longerPrefix = "/authorized";
        String path = longerPrefix + "/test-path";

        DocumentedEndpoint<?> endpoint = SwaggerEndpointStub.builder().path(path).build();

        given(gatewayMetaMock.getIgnoredPrefixes()).willReturn(List.of(prefix, longerPrefix));

        String returnedPath = EndpointUtil.pathWithRemovedPrefix(endpoint, gatewayMetaMock);

        String expectedPath = path.substring(longerPrefix.length());
        assertThat(returnedPath).isEqualTo(expectedPath);
    }

    @Test
    void pathWithRemovedPrefix_ifIgnoredPrefixSubstringOfFirstPathFragment_returnsUnchangedPath() {
        String prefix = "/auth";
        String path = "/authorized/test-path";

        DocumentedEndpoint<?> endpoint = SwaggerEndpointStub.builder().path(path).build();

        given(gatewayMetaMock.getIgnoredPrefixes()).willReturn(List.of(prefix));

        String returnedPath = EndpointUtil.pathWithRemovedPrefix(endpoint, gatewayMetaMock);
        assertThat(returnedPath).isEqualTo(path);
    }

    @Test
    void pathPrefix_ifNoPrefixesPassed_returnsEmptyString() {
        String prefix = "/auth";
        String path = prefix + "/test-path";

        DocumentedEndpoint<?> endpoint = SwaggerEndpointStub.builder().path(path).build();

        given(gatewayMetaMock.getIgnoredPrefixes()).willReturn(Collections.emptyList());

        String returnedPrefix = EndpointUtil.pathPrefix(endpoint, gatewayMetaMock);
        assertThat(returnedPrefix).isEqualTo("");
    }

    @Test
    void pathPrefix_ifPrefixContainedInReturnedCollectionOfIgnoredPrefixes_returnsMatchingPrefix() {
        String prefix = "/auth";
        String path = prefix + "/test-path";

        DocumentedEndpoint<?> endpoint = SwaggerEndpointStub.builder().path(path).build();

        given(gatewayMetaMock.getIgnoredPrefixes()).willReturn(List.of(prefix));

        String returnedPrefix = EndpointUtil.pathPrefix(endpoint, gatewayMetaMock);
        assertThat(returnedPrefix).isEqualTo(prefix);
    }

    @Test
    void pathPrefix_ifMultiplePrefixesMatch_returnsLongestMatchingPrefix() {
        String prefix = "/auth";
        String longerPrefix = "/authorized";
        String path = longerPrefix + "/test-path";

        DocumentedEndpoint<?> endpoint = SwaggerEndpointStub.builder().path(path).build();

        given(gatewayMetaMock.getIgnoredPrefixes()).willReturn(List.of(prefix, longerPrefix));

        String returnedPrefix = EndpointUtil.pathPrefix(endpoint, gatewayMetaMock);
        assertThat(returnedPrefix).isEqualTo(longerPrefix);
    }

    @Test
    void pathPrefix_ifIgnoredPrefixSubstringOfPathFragment_returnsEmptyString() {
        String prefix = "/auth";
        String path = "/authorized/test-path";

        DocumentedEndpoint<?> endpoint = SwaggerEndpointStub.builder().path(path).build();

        given(gatewayMetaMock.getIgnoredPrefixes()).willReturn(List.of(prefix));

        String returnedPath = EndpointUtil.pathPrefix(endpoint, gatewayMetaMock);
        assertThat(returnedPath).isEqualTo("");
    }
}