package com.example.dynamicgateway.model.documentedApplication;

import com.example.dynamicgateway.model.discoverableApplication.DiscoverableApplication;
import com.example.dynamicgateway.model.documentedEndpoint.DocumentedEndpoint;

import java.util.List;

/**
 * Application that exposes its API documentation, including available endpoints
 *
 * @param <T> type of document object of this {@code DocumentedApplication}
 */
public interface DocumentedApplication<T> {
    DiscoverableApplication getDiscoverableApp();

    String getDescription();

    List<? extends DocumentedEndpoint<?>> getEndpoints();

    T getNativeDoc();

    default String getName() {
        return getDiscoverableApp().getName();
    }
}
