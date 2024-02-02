package com.example.dynamicgateway.model.discoverableApplication;

/**
 * Application whose instances can be located by means of a discovery service
 *
 * @param <T> type of provider-specific application object
 */
public interface DiscoverableApplication<T> {
    String getName();

    String getDiscoveryServiceScheme();

    T unwrap();
}
