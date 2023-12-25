package com.example.dynamicgateway.service.endpointCollector;

import com.example.dynamicgateway.client.ApplicationDocClient;
import com.example.dynamicgateway.events.DocumentedEndpointFoundEvent;
import com.example.dynamicgateway.model.discoverableApplication.DiscoverableApplication;
import com.example.dynamicgateway.model.documentedApplication.SwaggerApplication;
import com.example.dynamicgateway.model.documentedEndpoint.SwaggerEndpoint;
import com.example.dynamicgateway.service.applicationFinder.ApplicationFinder;
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
import java.util.Set;

/**
 * Local cache of {@link SwaggerEndpoint}s
 */
@Component
@Slf4j
public class SwaggerEndpointCollector implements EndpointCollector<SwaggerEndpoint> {
    public final Set<SwaggerEndpoint> documentedEndpoints = new HashSet<>();
    public final ApplicationFinder applicationFinder;
    private final ApplicationDocClient<SwaggerParseResult> applicationDocClient;
    private final ApplicationEventPublisher eventPublisher;

    public SwaggerEndpointCollector(
            ApplicationFinder applicationFinder,
            ApplicationDocClient<SwaggerParseResult> applicationDocClient,
            ApplicationEventPublisher eventPublisher) {
        this.applicationFinder = applicationFinder;
        this.applicationDocClient = applicationDocClient;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Set<SwaggerEndpoint> getKnownEndpoints() {
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
                .subscribe(this::addEndpoint);
    }

    private void addEndpoint(SwaggerEndpoint endpoint) {
        boolean isEndpointAdded = documentedEndpoints.add(endpoint);
        if (isEndpointAdded) {
            log.info(MessageFormat.format(
                    "New endpoint found: {0}",
                    endpoint
            ));
            eventPublisher.publishEvent(new DocumentedEndpointFoundEvent(endpoint, this));
        }
    }
}
