package com.example.dynamicgateway.client;

import com.example.dynamicgateway.model.discoverableApplication.DiscoverableApplication;
import reactor.core.publisher.Mono;

/**
 * Client that can be used to find exposed API documentation of a {@link DiscoverableApplication}
 *
 * @param <T> type of documentation object supplied by an application
 */
public interface ApplicationDocClient<T> {
    /**
     * Returns a {@code Mono} of documentation object
     * @param application queried application
     */
    Mono<T> findApplicationDoc(DiscoverableApplication application);
}
