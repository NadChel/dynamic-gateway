package com.example.dynamicgateway.model.documentedEndpoint;

import com.example.dynamicgateway.model.documentedApplication.DocumentedApplication;
import com.example.dynamicgateway.model.endpointDetails.EndpointDetails;

/**
 * An endpoint exposed by a {@link DocumentedApplication} as part of its public API
 *
 * @param <A> type of {@code DocumentedApplication} that publishes this endpoint
 */
public interface DocumentedEndpoint<A extends DocumentedApplication<?>> {
    A getDeclaringApp();

    EndpointDetails getDetails();
}
