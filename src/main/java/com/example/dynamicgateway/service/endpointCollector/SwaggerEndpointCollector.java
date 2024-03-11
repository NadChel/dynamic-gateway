package com.example.dynamicgateway.service.endpointCollector;

import com.example.dynamicgateway.events.DiscoverableApplicationFoundEvent;
import com.example.dynamicgateway.events.DiscoverableApplicationLostEvent;
import com.example.dynamicgateway.events.DocumentedEndpointFoundEvent;
import com.example.dynamicgateway.model.discoverableApplication.DiscoverableApplication;
import com.example.dynamicgateway.model.documentedApplication.SwaggerApplication;
import com.example.dynamicgateway.model.documentedEndpoint.DocumentedEndpoint;
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
import java.util.concurrent.ConcurrentHashMap;

/**
 * A subtype of {@link AbstractFilteringEndpointCollector} that collects {@link SwaggerEndpoint}s
 */
@Component
@Slf4j
public class SwaggerEndpointCollector extends AbstractFilteringEndpointCollector<SwaggerEndpoint> {
    private final ApplicationDocClient<SwaggerParseResult> applicationDocClient;
    private final ApplicationEventPublisher eventPublisher;

    public SwaggerEndpointCollector(
            ApplicationDocClient<SwaggerParseResult> applicationDocClient,
            List<EndpointSieve> endpointSieves,
            ApplicationEventPublisher eventPublisher) {
        super(ConcurrentHashMap::newKeySet, endpointSieves);
        this.applicationDocClient = applicationDocClient;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Fetches and collects all allowed endpoints exposed by a found {@link DiscoverableApplication}.
     * <p>
     * This method internally calls {@link AbstractFilteringEndpointCollector#addEndpoint(DocumentedEndpoint)}.
     * In case the method returns {@code true}, a {@link DocumentedEndpointFoundEvent} is published
     *
     * @param event event that contains a found {@code DiscoverableApplication}
     */
    @EventListener
    public void onDiscoverableApplicationFoundEvent(DiscoverableApplicationFoundEvent event) {
        log.info("onDiscoverableApplicationFoundEvent() triggered");
        DiscoverableApplication<?> foundService = event.getFoundApp();
        collectAllowedEndpoints(foundService);
    }

    private void collectAllowedEndpoints(DiscoverableApplication<?> application) {
        applicationDocClient.findApplicationDoc(application)
                .map(applicationDoc -> new SwaggerApplication(application, applicationDoc))
                .flatMapIterable(SwaggerApplication::getEndpoints)
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("{}'s OpenAPI contains no endpoints", application.getName());
                    return Mono.empty();
                }))
                .filter(this::addEndpoint)
                .subscribe(this::publishDocumentedEndpointFoundEvent);
    }

    private void publishDocumentedEndpointFoundEvent(SwaggerEndpoint endpoint) {
        eventPublisher.publishEvent(
                new DocumentedEndpointFoundEvent(endpoint, this));
    }

    /**
     * Clears this {@code EndpointCollector}'s collection of all endpoints declared by
     * the lost {@link DiscoverableApplication}. More formally, it removes all endpoints
     * whose <em>declaring applications</em> wrap a {@code DiscoverableApplication}
     * <em>equal</em> to the {@code DiscoverableApplication} returned by the event's
     * {@link DiscoverableApplicationLostEvent#getLostApp()} method
     *
     * @param event event that contains a lost {@code DiscoverableApplication}
     * @see DocumentedEndpoint#getDeclaringApp()
     */
    @EventListener
    public void onDiscoverableApplicationLostEvent(DiscoverableApplicationLostEvent event) {
        log.info("onDiscoverableApplicationLostEvent() triggered");
        DiscoverableApplication<?> lostService = event.getLostApp();
        evictEndpointsOf(lostService);
    }

    private void evictEndpointsOf(DiscoverableApplication<?> lostService) {
        for (Iterator<SwaggerEndpoint> iterator = documentedEndpoints.iterator(); iterator.hasNext(); ) {
            SwaggerEndpoint endpoint = iterator.next();
            DiscoverableApplication<?> endpointsDiscoverableApp = endpoint.getDeclaringApp().getDiscoverableApp();
            if (endpointsDiscoverableApp.equals(lostService)) {
                iterator.remove();
                log.info("Endpoint {} exposed by lost {} was evicted", endpoint, lostService.getName());
            }
        }
    }
}
