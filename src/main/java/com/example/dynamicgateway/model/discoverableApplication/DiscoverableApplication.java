package com.example.dynamicgateway.model.discoverableApplication;

/**
 * Application whose instances can be located by means of a discovery service
 *
 * @param <T> type of provider-specific application object
 */
public interface DiscoverableApplication<T> {
    String getName();

    String getDiscoveryServiceScheme();

    /**
     * Returns a provider-specific application object wrapped by this {@code DiscoverableApplication}
     */
    T unwrap();
}
