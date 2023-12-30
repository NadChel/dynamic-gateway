package com.example.dynamicgateway.service.endpointCollector;

import com.example.dynamicgateway.model.documentedEndpoint.DocumentedEndpoint;
import org.springframework.http.HttpMethod;

import java.util.Set;

/**
 * Local cache of all known {@link DocumentedEndpoint}s
 *
 * @param <T> type of {@link DocumentedEndpoint}s that are collected by this {@code EndpointCollector}
 */
public interface EndpointCollector<T extends DocumentedEndpoint<?>> {
    /**
     * Returns a {@code Set} of all {@link DocumentedEndpoint}s cached by this {@code EndpointCollector}
     */
    Set<T> getCollectedEndpoints();

    boolean hasEndpoint(HttpMethod method, String path);
}
