package com.example.dynamicgateway.service.applicationCollector;

import com.example.dynamicgateway.model.discoverableApplication.EurekaDiscoverableApplication;
import com.example.dynamicgateway.service.endpointSieve.DiscoverableApplicationSieve;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.shared.Application;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EurekaApplicationCollectorTest {
    @Test
    void whenCreated_hasNoApps() {
        EurekaClient eurekaClientMock = mock(EurekaClient.class);

        ApplicationEventPublisher eventPublisherMock = mock(ApplicationEventPublisher.class);

        EurekaApplicationCollector collector =
                new EurekaApplicationCollector(eurekaClientMock, Collections.emptyList(), eventPublisherMock);

        assertThat(collector.getCollectedApplications()).isEmpty();
    }

    @Test
    void testOnApplicationReadyEvent() {
        testCacheRefreshingMethod(EurekaApplicationCollector::onApplicationReadyEvent);
    }

    @Test
    void testOnCacheRefreshedEvent() {
        testCacheRefreshingMethod(EurekaApplicationCollector::onCacheRefreshedEvent);
    }

    private void testCacheRefreshingMethod(Consumer<EurekaApplicationCollector> method) {
        Application app = new Application("test-app-1");
        Application anotherApp = new Application("test-app-2");
        Application phonyApp = new Application("phony-app");

        EurekaClient eurekaClientMock = mock(EurekaClient.class, RETURNS_DEEP_STUBS);
        when(eurekaClientMock.getApplications().getRegisteredApplications()).thenReturn(List.of(
                app, anotherApp, phonyApp
        ));

        List<DiscoverableApplicationSieve> sieves =
                List.of(a -> !a.getName().startsWith("phony"));

        ApplicationEventPublisher eventPublisherMock = mock(ApplicationEventPublisher.class);

        EurekaApplicationCollector collector =
                new EurekaApplicationCollector(eurekaClientMock, sieves, eventPublisherMock);

        assumeThat(collector.getCollectedApplications()).isEmpty();

        method.accept(collector);

        Set<EurekaDiscoverableApplication> actualApplications = collector.getCollectedApplications();

        Set<EurekaDiscoverableApplication> expectedApplications = Stream.of(app, anotherApp)
                .map(EurekaDiscoverableApplication::from)
                .collect(Collectors.toSet());

        assertThat(actualApplications).containsExactlyInAnyOrderElementsOf(expectedApplications);
    }
}