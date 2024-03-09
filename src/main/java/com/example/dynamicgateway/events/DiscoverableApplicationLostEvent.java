package com.example.dynamicgateway.events;

import com.example.dynamicgateway.model.discoverableApplication.DiscoverableApplication;

/**
 * A {@link DiscoverableApplicationEvent} indicating unavailability of a previously discovered application
 */
public class DiscoverableApplicationLostEvent extends DiscoverableApplicationEvent {
    public DiscoverableApplicationLostEvent(DiscoverableApplication<?> app, Object source) {
        super(app, source);
    }

    public DiscoverableApplication<?> getLostApp() {
        return app;
    }
}
