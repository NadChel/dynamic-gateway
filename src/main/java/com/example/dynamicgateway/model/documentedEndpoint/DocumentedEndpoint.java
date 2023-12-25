package by.afinny.apigateway.model.documentedEndpoint;

import by.afinny.apigateway.model.documentedApplication.DocumentedApplication;
import by.afinny.apigateway.model.endpointDetails.EndpointDetails;

/**
 * Endpoint exposed by a {@link DocumentedApplication} as part of its public API
 *
 * @param <T> type of {@link DocumentedApplication} that exposes this endpoint
 */
public interface DocumentedEndpoint<T extends DocumentedApplication<?>> {
    T getDeclaringApp();

    EndpointDetails getDetails();
}
