package com.example.dynamicgateway.service.endpointSieve;

import com.example.dynamicgateway.model.documentedEndpoint.DocumentedEndpoint;

public interface EndpointSieve {
    boolean isAllowed(DocumentedEndpoint<?> endpoint);
}
