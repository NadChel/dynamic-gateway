package com.example.dynamicgateway.model.documentedEndpoint;

import com.example.dynamicgateway.model.documentedApplication.DocumentedApplication;
import com.example.dynamicgateway.model.endpointDetails.EndpointDetails;

/**
 * Endpoint exposed by a {@link DocumentedApplication} as part of its public API
 *
 * @param <T> type of {@link DocumentedApplication} that exposes this endpoint
 */
public interface DocumentedEndpoint<T extends DocumentedApplication<?>> {
    T getDeclaringApp();

    EndpointDetails getDetails();
}
