package com.example.dynamicgateway.service.applicationCollector;

import com.example.dynamicgateway.events.DiscoverableApplicationFoundEvent;
import com.example.dynamicgateway.events.DiscoverableApplicationLostEvent;
import com.example.dynamicgateway.model.discoverableApplication.EurekaDiscoverableApplication;
import com.example.dynamicgateway.service.endpointSieve.DiscoverableApplicationSieve;
import com.netflix.discovery.CacheRefreshedEvent;
import com.netflix.discovery.EurekaClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * {@link ApplicationCollector} that relies on Netflix Eureka as its discovery service
 */
@Slf4j
@Component
public class EurekaApplicationCollector implements ApplicationCollector {
    private final EurekaClient eurekaClient;
    private final ApplicationEventPublisher eventPublisher;
    private final Set<EurekaDiscoverableApplication> discoveredApplications = new HashSet<>();
    private final List<DiscoverableApplicationSieve> applicationSieves;

    public EurekaApplicationCollector(EurekaClient eurekaClient,
                                      List<DiscoverableApplicationSieve> applicationSieves,
                                      ApplicationEventPublisher publisher) {
        this.applicationSieves = applicationSieves;
        this.eventPublisher = publisher;
        this.eurekaClient = eurekaClient;
        this.eurekaClient.registerEventListener(event -> {
            if (event instanceof CacheRefreshedEvent cacheRefreshedEvent) {
                publisher.publishEvent(cacheRefreshedEvent);
            }
        });
    }

    @Override
    public Set<EurekaDiscoverableApplication> getCollectedApplications() {
        return Set.copyOf(discoveredApplications);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReadyEvent() {
        refreshAppCache();
    }

    @EventListener(CacheRefreshedEvent.class)
    public void onCacheRefreshedEvent() {
        refreshAppCache();
    }

    private void refreshAppCache() {
        List<EurekaDiscoverableApplication> retainedCachedApps = eurekaClient
                .getApplications()
                .getRegisteredApplications()
                .stream()
                .map(EurekaDiscoverableApplication::from)
                .filter(this::passesThroughSieves)
                .toList();

        for (EurekaDiscoverableApplication app : retainedCachedApps) {
            if (!discoveredApplications.contains(app)) {
                discoveredApplications.add(app);
                log.info("New service found: {}", app.getName());
                DiscoverableApplicationFoundEvent event = new DiscoverableApplicationFoundEvent(app, this);
                eventPublisher.publishEvent(event);
            }
        }

        for (Iterator<EurekaDiscoverableApplication> iterator = discoveredApplications.iterator(); iterator.hasNext(); ) {
            EurekaDiscoverableApplication app = iterator.next();
            if (!retainedCachedApps.contains(app)) {
                iterator.remove();
                log.warn("Service lost: {}", app.getName());
                DiscoverableApplicationLostEvent event = new DiscoverableApplicationLostEvent(app, this);
                eventPublisher.publishEvent(event);
            }
        }
    }

    private boolean passesThroughSieves(EurekaDiscoverableApplication app) {
        return applicationSieves.stream().allMatch(sieve -> sieve.isAllowed(app));
    }
}
