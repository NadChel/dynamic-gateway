package com.example.dynamicgateway.service.endpointCollector;

import com.example.dynamicgateway.events.DocumentedEndpointFoundEvent;
import com.example.dynamicgateway.model.discoverableApplication.DiscoverableApplication;
import com.example.dynamicgateway.model.documentedApplication.SwaggerApplication;
import com.example.dynamicgateway.model.documentedEndpoint.SwaggerEndpoint;
import com.example.dynamicgateway.service.applicationDocClient.ApplicationDocClient;
import com.example.dynamicgateway.service.applicationFinder.ApplicationFinder;
import com.example.dynamicgateway.service.endpointSieve.EndpointSieve;
import com.netflix.discovery.CacheRefreshedEvent;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Local cache of {@link SwaggerEndpoint}s
 */
@Component
@Slf4j
public class SwaggerEndpointCollector implements EndpointCollector<SwaggerEndpoint> {
    private final Set<SwaggerEndpoint> documentedEndpoints = new HashSet<>();
    private final ApplicationFinder applicationFinder;
    private final ApplicationDocClient<SwaggerParseResult> applicationDocClient;
    private final ApplicationEventPublisher eventPublisher;
    private final List<EndpointSieve> endpointSieves;

    public SwaggerEndpointCollector(
            ApplicationFinder applicationFinder,
            ApplicationDocClient<SwaggerParseResult> applicationDocClient,
            ApplicationEventPublisher eventPublisher,
            List<EndpointSieve> endpointSieves) {
        this.applicationFinder = applicationFinder;
        this.applicationDocClient = applicationDocClient;
        this.eventPublisher = eventPublisher;
        this.endpointSieves = endpointSieves;
    }

    @Override
    public Set<SwaggerEndpoint> getCollectedEndpoints() {
        return documentedEndpoints;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReadyEvent() {
        log.info("onApplicationReadyEvent() triggered");
        refreshEndpoints();
    }

    @EventListener(CacheRefreshedEvent.class)
    public void onCacheRefreshedEvent() {
        log.info("onCacheRefreshedEvent() triggered");
        refreshEndpoints();
    }

    private void refreshEndpoints() {
        log.info("Endpoint discovery started");
        Set<? extends DiscoverableApplication> applications = applicationFinder.findOtherRegisteredApplications();
        applications.forEach(this::subscribeToDocs);
    }

    private void subscribeToDocs(DiscoverableApplication application) {
        Mono<SwaggerParseResult> appDocMono = applicationDocClient.findApplicationDoc(application);
        appDocMono
                .map(applicationDoc -> new SwaggerApplication(application, applicationDoc))
                .flatMapIterable(SwaggerApplication::getEndpoints)
                .filter(this::passesThroughSieves)
                .subscribe(this::addEndpoint);
    }

    private boolean passesThroughSieves(SwaggerEndpoint endpoint) {
        return endpointSieves.stream()
                .allMatch(endpointSieve -> endpointSieve.isAllowed(endpoint));
    }

    private void addEndpoint(SwaggerEndpoint endpoint) {
        boolean isEndpointAdded = documentedEndpoints.add(endpoint);
        if (isEndpointAdded) {
            log.info(MessageFormat.format(
                    "New endpoint is collected: {0}",
                    endpoint
            ));
            eventPublisher.publishEvent(new DocumentedEndpointFoundEvent(endpoint, this));
        }
    }
}
