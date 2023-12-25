package com.example.dynamicgateway.service.applicationFinder;


import com.example.dynamicgateway.model.discoverableApplication.DiscoverableApplication;

import java.util.Set;

/**
 * Interface implemented by classes that can fetch applications registered with the same discovery service
 * as this application
 */
public interface ApplicationFinder {
    /**
     * Returns a {@code Set} of {@link DiscoverableApplication}s excluding this application
     */
    Set<? extends DiscoverableApplication> findOtherRegisteredApplications();
}
