package com.example.dynamicgateway.events;

import com.example.dynamicgateway.model.documentedEndpoint.DocumentedEndpoint;

/**
 * A {@link DocumentedEndpointEvent} indicating a discovery of a new {@link DocumentedEndpoint}
 */
public class DocumentedEndpointFoundEvent extends DocumentedEndpointEvent {
    public DocumentedEndpointFoundEvent(DocumentedEndpoint<?> foundEndpoint, Object source) {
        super(foundEndpoint, source);
    }

    public DocumentedEndpoint<?> getFoundEndpoint() {
        return endpoint;
    }
}
