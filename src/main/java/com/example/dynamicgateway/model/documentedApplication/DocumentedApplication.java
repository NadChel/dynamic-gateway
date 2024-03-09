package com.example.dynamicgateway.model.documentedApplication;

import com.example.dynamicgateway.model.discoverableApplication.DiscoverableApplication;
import com.example.dynamicgateway.model.documentedEndpoint.DocumentedEndpoint;

import java.util.List;

/**
 * An application that exposes its API documentation, including publicly available endpoints
 *
 * @param <D> type of document object of this {@code DocumentedApplication}
 */
public interface DocumentedApplication<D> {
    DiscoverableApplication<?> getDiscoverableApp();

    String getDescription();

    List<? extends DocumentedEndpoint<?>> getEndpoints();

    D getNativeDoc();

    default String getName() {
        return getDiscoverableApp().getName();
    }
}
