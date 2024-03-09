package com.example.dynamicgateway.events;

import com.example.dynamicgateway.model.discoverableApplication.DiscoverableApplication;

/**
 * A {@link DiscoverableApplicationEvent} indicating a discovery of a new application
 */
public class DiscoverableApplicationFoundEvent extends DiscoverableApplicationEvent {
    public DiscoverableApplicationFoundEvent(DiscoverableApplication<?> foundApp, Object source) {
        super(foundApp, source);
    }

    public DiscoverableApplication<?> getFoundApp() {
        return app;
    }
}
