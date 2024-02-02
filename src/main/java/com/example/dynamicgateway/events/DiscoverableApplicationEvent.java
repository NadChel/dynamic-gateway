package com.example.dynamicgateway.events;

import com.example.dynamicgateway.model.discoverableApplication.DiscoverableApplication;
import org.springframework.context.ApplicationEvent;

/**
 * {@link ApplicationEvent} concerning a {@link DiscoverableApplication}
 */
public abstract class DiscoverableApplicationEvent extends ApplicationEvent {
    protected final DiscoverableApplication<?> app;
    public DiscoverableApplicationEvent(DiscoverableApplication<?> app, Object source) {
        super(source);
        this.app = app;
    }
}
