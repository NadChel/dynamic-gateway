package com.example.dynamicgateway.service.applicationCollector;


import com.example.dynamicgateway.model.discoverableApplication.DiscoverableApplication;

import java.util.Set;

/**
 * Local cache of {@link DiscoverableApplication}s registered with the same discovery service
 * as this application
 */
public interface ApplicationCollector {
    /**
     * Returns a {@code Set} of {@link DiscoverableApplication}s stored by this collector
     */
    Set<? extends DiscoverableApplication<?>> getCollectedApplications();
}
