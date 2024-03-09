package com.example.dynamicgateway.model.discoverableApplication;

/**
 * An application whose instances can be located by means of a discovery service
 *
 * @param <A> type of provider-specific application object
 */
public interface DiscoverableApplication<A> {
    String getName();

    String getDiscoveryServiceScheme();

    /**
     * Returns a provider-specific application object wrapped by this {@code DiscoverableApplication}
     */
    A unwrap();
}
