package com.example.dynamicgateway.service.applicationDocClient;

import com.example.dynamicgateway.model.discoverableApplication.DiscoverableApplication;
import reactor.core.publisher.Mono;

/**
 * A client that can be used to find exposed API documentation of a {@link DiscoverableApplication}
 *
 * @param <D> type of documentation object supplied by an application
 */
public interface ApplicationDocClient<D> {
    /**
     * Returns a {@code Mono} of documentation object
     * @param application queried application
     */
    Mono<D> findApplicationDoc(DiscoverableApplication<?> application);
}
