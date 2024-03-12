package com.example.dynamicgateway.service.endpointCollector;

import com.example.dynamicgateway.model.documentedEndpoint.DocumentedEndpoint;
import com.example.dynamicgateway.model.endpointDetails.EndpointDetails;
import org.springframework.http.HttpMethod;

import java.util.Set;
import java.util.stream.Stream;

/**
 * A local cache of all collected {@link DocumentedEndpoint}s
 * <p>
 * Implementations are free to decide what endpoints they collect.
 * It may or may not include all <em>accessible</em> endpoints
 *
 * @param <E> type of {@link DocumentedEndpoint}s that are collected by this {@code EndpointCollector}
 */
public interface EndpointCollector<E extends DocumentedEndpoint<?>> {
    /**
     * Returns a {@code Set} of all {@link DocumentedEndpoint}s collected by this {@code EndpointCollector}
     */
    Set<E> getCollectedEndpoints();

    /**
     * Tests if the provided method-path pair corresponds to at least one endpoint
     * collected by this {@code EndpointCollector}
     *
     * @return {@code true} if a match is found, {@code false} otherwise
     */
    default boolean hasEndpoint(HttpMethod method, String path) {
        return stream().anyMatch(endpoint -> {
            EndpointDetails details = endpoint.getDetails();
            return details.getMethod().equals(method) &&
                    details.getPath().equals(path);
        });
    }

    /**
     * Returns a {@link Stream} of {@code DocumentedEndpoint}s collected by this
     * {@code EndpointCollector}
     */
    default Stream<E> stream() {
        return getCollectedEndpoints().stream();
    }
}
