package com.example.dynamicgateway.events;

import com.example.dynamicgateway.model.documentedEndpoint.DocumentedEndpoint;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event published to indicate a discovery of a new {@link DocumentedEndpoint}
 */
@Getter
public class DocumentedEndpointFoundEvent extends ApplicationEvent {
    private final DocumentedEndpoint<?> foundEndpoint;

    public DocumentedEndpointFoundEvent(DocumentedEndpoint<?> foundEndpoint, Object source) {
        super(source);
        this.foundEndpoint = foundEndpoint;
    }
}
