package com.example.dynamicgateway.service.endpointCollector;

import com.example.dynamicgateway.model.documentedEndpoint.DocumentedEndpoint;
import com.example.dynamicgateway.service.sieve.EndpointSieve;
import com.example.dynamicgateway.testModel.SwaggerEndpointStub;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class AbstractFilteringEndpointCollectorTest {
    @Test
    @SuppressWarnings("unchecked")
    void addEndpoint_ifEndpointPassesThroughSieves_butCollectionReturnsFalse_returnsFalse() {
        Collection<DocumentedEndpoint<?>> collectionMock = mock(Collection.class);
        given(collectionMock.add(any())).willReturn(false);
        List<EndpointSieve> sieves = List.of(e -> true);
        var collector = new AbstractFilteringEndpointCollector<>(() -> collectionMock, sieves) {
        };
        SwaggerEndpointStub endpoint = SwaggerEndpointStub.builder().build();
        assertThat(collector.addEndpoint(endpoint)).isFalse();
    }
}