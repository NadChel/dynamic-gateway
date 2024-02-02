package com.example.dynamicgateway.events;

import com.example.dynamicgateway.model.documentedEndpoint.DocumentedEndpoint;
import org.springframework.context.ApplicationEvent;

/**
 * {@link ApplicationEvent} concerning a {@link DocumentedEndpoint}
 */
public abstract class DocumentedEndpointEvent extends ApplicationEvent {
    protected final DocumentedEndpoint<?> endpoint;
    public DocumentedEndpointEvent(DocumentedEndpoint<?> endpoint, Object source) {
        super(source);
        this.endpoint = endpoint;
    }
}
