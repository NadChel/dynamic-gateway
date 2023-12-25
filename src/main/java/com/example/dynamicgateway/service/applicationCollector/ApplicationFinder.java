package by.afinny.apigateway.service.applicationCollector;

import by.afinny.apigateway.model.discoverableApplication.DiscoverableApplication;

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
