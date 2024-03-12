package com.example.dynamicgateway.service.sieve;

import com.example.dynamicgateway.model.documentedEndpoint.DocumentedEndpoint;

/**
 * A {@link Sieve} that filters {@link DocumentedEndpoint}s
 */
public interface EndpointSieve extends Sieve<DocumentedEndpoint<?>> {
}
