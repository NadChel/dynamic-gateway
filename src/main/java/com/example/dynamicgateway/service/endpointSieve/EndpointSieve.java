package com.example.dynamicgateway.service.endpointSieve;

import com.example.dynamicgateway.model.documentedEndpoint.DocumentedEndpoint;

/**
 * In effect, a glorified {@code Predicate<DocumentedEndpoint<?>>} that serves to filter out unwanted endpoints
 */
public interface EndpointSieve {
    boolean isAllowed(DocumentedEndpoint<?> endpoint);
}
