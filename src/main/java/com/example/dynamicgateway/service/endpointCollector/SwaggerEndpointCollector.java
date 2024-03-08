package com.example.dynamicgateway.service.endpointCollector;

import com.example.dynamicgateway.events.DiscoverableApplicationFoundEvent;
import com.example.dynamicgateway.events.DiscoverableApplicationLostEvent;
import com.example.dynamicgateway.events.DocumentedEndpointFoundEvent;
import com.example.dynamicgateway.model.discoverableApplication.DiscoverableApplication;
import com.example.dynamicgateway.model.documentedApplication.SwaggerApplication;
import com.example.dynamicgateway.model.documentedEndpoint.SwaggerEndpoint;
import com.example.dynamicgateway.service.applicationDocClient.ApplicationDocClient;
import com.example.dynamicgateway.service.sieve.EndpointSieve;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Local cache of {@link SwaggerEndpoint}s
 */
@Component
@Slf4j
public class SwaggerEndpointCollector implements EndpointCollector<SwaggerEndpoint> {
    private final Set<SwaggerEndpoint> documentedEndpoints = ConcurrentHashMap.newKeySet();
    private final ApplicationDocClient<SwaggerParseResult> applicationDocClient;
    private final ApplicationEventPublisher eventPublisher;
    private final List<EndpointSieve> endpointSieves;

    public SwaggerEndpointCollector(
            ApplicationDocClient<SwaggerParseResult> applicationDocClient,
            List<EndpointSieve> endpointSieves,
            ApplicationEventPublisher eventPublisher) {
        this.applicationDocClient = applicationDocClient;
        this.eventPublisher = eventPublisher;
        this.endpointSieves = endpointSieves;
    }

    @Override
    public Set<SwaggerEndpoint> getCollectedEndpoints() {
        return Set.copyOf(documentedEndpoints);
    }

    @EventListener
    public void onDiscoverableApplicationFoundEvent(DiscoverableApplicationFoundEvent event) {
        log.info("onDiscoverableApplicationFoundEvent() triggered");
        DiscoverableApplication<?> foundService = event.getFoundApp();
        subscribeToDocs(foundService);
    }

    private void subscribeToDocs(DiscoverableApplication<?> application) {
        applicationDocClient.findApplicationDoc(application)
                .map(applicationDoc -> new SwaggerApplication(application, applicationDoc))
                .flatMapIterable(SwaggerApplication::getEndpoints)
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("{}'s OpenAPI contains no endpoints", application.getName());
                    return Mono.empty();
                }))
                .filter(this::passesThroughSieves)
                .subscribe(this::addEndpoint);
    }

    private boolean passesThroughSieves(SwaggerEndpoint endpoint) {
        return endpointSieves.stream().allMatch(sieve -> sieve.isAllowed(endpoint));
    }

    private void addEndpoint(SwaggerEndpoint endpoint) {
        boolean isEndpointAdded = documentedEndpoints.add(endpoint);
        if (isEndpointAdded) {
            log.info("New endpoint collected: {}", endpoint);
            eventPublisher.publishEvent(new DocumentedEndpointFoundEvent(endpoint, this));
        }
    }

    @EventListener
    public void onDiscoverableApplicationLostEvent(DiscoverableApplicationLostEvent event) {
        log.info("onDiscoverableApplicationLostEvent() triggered");
        DiscoverableApplication<?> lostService = event.getLostApp();
        evictEndpointsOf(lostService);
    }

    private void evictEndpointsOf(DiscoverableApplication<?> lostService) {
        for (Iterator<SwaggerEndpoint> iterator = documentedEndpoints.iterator(); iterator.hasNext(); ) {
            SwaggerEndpoint endpoint = iterator.next();
            if (endpoint.getDeclaringApp().getDiscoverableApp().equals(lostService)) {
                iterator.remove();
                log.info("Endpoint {} exposed by lost {} was evicted", endpoint, lostService.getName());
            }
        }
    }
}
