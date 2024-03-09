package com.example.dynamicgateway.service.applicationCollector;


import com.example.dynamicgateway.model.discoverableApplication.DiscoverableApplication;

import java.util.Set;

/**
 * A local cache of {@link DiscoverableApplication}s registered with the same discovery service
 * as this Gateway
 */
public interface ApplicationCollector {
    /**
     * Returns a {@code Set} of {@link DiscoverableApplication}s stored by this collector
     */
    Set<? extends DiscoverableApplication<?>> getCollectedApplications();
}
