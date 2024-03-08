package com.example.dynamicgateway.service.applicationCollector;

import com.example.dynamicgateway.model.discoverableApplication.EurekaDiscoverableApplication;
import com.example.dynamicgateway.service.sieve.DiscoverableApplicationSieve;
import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.CacheRefreshedEvent;
import com.netflix.discovery.DiscoveryClient;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.EurekaClientConfig;
import com.netflix.discovery.EurekaEvent;
import com.netflix.discovery.StatusChangeEvent;
import com.netflix.discovery.shared.Application;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.verification.VerificationMode;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class EurekaApplicationCollectorTest {
    @Mock
    private EurekaClient eurekaClientMock;
    @Mock
    private ApplicationEventPublisher eventPublisherMock;

    @Test
    void whenCreated_hasNoApps() {
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

        eurekaClientMock = mock(EurekaClient.class, RETURNS_DEEP_STUBS);
        given(eurekaClientMock.getApplications().getRegisteredApplications()).willReturn(List.of(
                app, anotherApp, phonyApp
        ));

        List<DiscoverableApplicationSieve> sieves =
                List.of(a -> !a.getName().startsWith("phony"));

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

    @Test
    void onCacheRefreshEvents_passedInListenerRethrowsThemViaSpringEventPublisher() {
        assertEventPublished(new CacheRefreshedEvent(), times(1));
    }

    @Test
    void onEurekaEventsThatAreNotCacheRefreshedEvents_passedInListenerDoesntRethrowThemViaSpringEventPublisher() {
        StatusChangeEvent notCacheRefreshedEvent = new StatusChangeEvent(InstanceInfo.InstanceStatus.UP, InstanceInfo.InstanceStatus.DOWN);
        assertEventPublished(notCacheRefreshedEvent, never());
    }

    @SuppressWarnings("DataFlowIssue")
    private void assertEventPublished(EurekaEvent event, VerificationMode times) {
        ApplicationInfoManager appManagerMock = mock(ApplicationInfoManager.class);
        EurekaClientConfig eurekaClientConfigMock = mock(EurekaClientConfig.class);
        EurekaClient eurekaClient = new DiscoveryClient(appManagerMock, eurekaClientConfigMock, null);

        new EurekaApplicationCollector(eurekaClient, Collections.emptyList(), eventPublisherMock);

        Method fireEventMethod = ReflectionUtils.findMethod(DiscoveryClient.class, "fireEvent", EurekaEvent.class);
        assumeThat(fireEventMethod).isNotNull();
        fireEventMethod.setAccessible(true);
        ReflectionUtils.invokeMethod(fireEventMethod, eurekaClient, event);
        then(eventPublisherMock).should(times).publishEvent(event);
    }
}