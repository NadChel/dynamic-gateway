package com.example.dynamicgateway.service.endpointCollector;

import com.example.dynamicgateway.model.documentedEndpoint.DocumentedEndpoint;
import com.example.dynamicgateway.service.sieve.EndpointSieve;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Set;
import java.util.function.Supplier;

/**
 * An {@link EndpointCollector} that applies its injected {@link EndpointSieve}s on any endpoint
 * suggested for collection
 */
@Slf4j
public abstract class AbstractFilteringEndpointCollector<E extends DocumentedEndpoint<?>> implements EndpointCollector<E> {
    protected final Collection<E> documentedEndpoints;
    private final Collection<? extends EndpointSieve> endpointSieves;

    protected AbstractFilteringEndpointCollector(Supplier<Collection<E>> endpointCollectionSupplier,
                                                 Collection<? extends EndpointSieve> endpointSieves) {
        this.documentedEndpoints = endpointCollectionSupplier.get();
        this.endpointSieves = endpointSieves;
    }

    /**
     * @return an unmodifiable {@code Set} that includes all the endpoints
     * collected by this {@code EndpointCollector}
     */
    @Override
    public Set<E> getCollectedEndpoints() {
        return Set.copyOf(documentedEndpoints);
    }

    /**
     * Passes a given endpoint through this {@code EndpointCollector}'s {@code EndpointSieve}s
     * and then, if the element was retained, tries to add the argument to the endpoint collection
     *
     * @param endpoint endpoint that should be considered for addition
     * @return {@code true} if both of these conditions are met: 1) all the sieves returned
     * {@code true} on the argument; 2) the endpoint collection returned {@code true} when
     * its {@link Collection#add(Object)} method was invoked on the argument. Otherwise, this
     * method returns {@code false}
     */
    protected boolean addEndpoint(E endpoint) {
        boolean isEndpointAdded = passesThroughSieves(endpoint) &&
                documentedEndpoints.add(endpoint);
        if (isEndpointAdded) {
            log.info("New endpoint collected: {}", endpoint);
        }
        return isEndpointAdded;
    }

    private boolean passesThroughSieves(E endpoint) {
        return endpointSieves.stream().allMatch(sieve -> sieve.isAllowed(endpoint));
    }
}
