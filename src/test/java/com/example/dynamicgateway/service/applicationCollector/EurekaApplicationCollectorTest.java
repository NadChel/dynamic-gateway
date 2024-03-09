package com.example.dynamicgateway.service.applicationCollector;

import com.example.dynamicgateway.events.DiscoverableApplicationFoundEvent;
import com.example.dynamicgateway.events.DiscoverableApplicationLostEvent;
import com.example.dynamicgateway.model.discoverableApplication.DiscoverableApplication;
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
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.verification.VerificationMode;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.MessageFormat;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class EurekaApplicationCollectorTest {
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
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
    void onCacheRefreshEvents_passedInListenerRethrowsThemViaSpringEventPublisher() {
        assertEventPublished(new CacheRefreshedEvent(), times(1));
    }

    @Test
    void onEurekaEventsThatAreNotCacheRefreshedEvents_passedInListenerDoesntRethrowThemViaSpringEventPublisher() {
        StatusChangeEvent notCacheRefreshedEvent = new StatusChangeEvent(InstanceInfo.InstanceStatus.UP,
                InstanceInfo.InstanceStatus.DOWN);
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

    @Test
    void onApplicationReadyEvent_enrichesCacheWithNewApps() {
        assertCacheEnrichedOn(EurekaApplicationCollector::onApplicationReadyEvent);
    }

    @Test
    void onCacheRefreshedEvent_enrichesCacheWithNewApps() {
        assertCacheEnrichedOn(EurekaApplicationCollector::onCacheRefreshedEvent);
    }

    private void assertCacheEnrichedOn(Consumer<EurekaApplicationCollector> refreshingMethod) {
        Application app = new Application("test-app-1");
        Application anotherApp = new Application("test-app-2");
        Application phonyApp = new Application("phony-app");

        given(eurekaClientMock.getApplications().getRegisteredApplications()).willReturn(List.of(
                app, anotherApp, phonyApp
        ));

        List<DiscoverableApplicationSieve> sieves =
                List.of(a -> !a.getName().startsWith("phony"));

        EurekaApplicationCollector collector =
                new EurekaApplicationCollector(eurekaClientMock, sieves, eventPublisherMock);
        assumeThat(collector.getCollectedApplications()).isEmpty();

        refreshingMethod.accept(collector);

        Set<EurekaDiscoverableApplication> actualApplications = collector.getCollectedApplications();

        Set<EurekaDiscoverableApplication> expectedApplications = Stream.of(app, anotherApp)
                .map(EurekaDiscoverableApplication::from)
                .collect(Collectors.toSet());

        assertThat(actualApplications).containsExactlyInAnyOrderElementsOf(expectedApplications);

        ArgumentCaptor<DiscoverableApplicationFoundEvent> eventCaptor =
                ArgumentCaptor.forClass(DiscoverableApplicationFoundEvent.class);
        then(eventPublisherMock).should(times(2)).publishEvent(eventCaptor.capture());
        List<String> expectedApplicationsNames = expectedApplications.stream()
                .map(EurekaDiscoverableApplication::getName)
                .toList();
        assertThat(eventCaptor.getAllValues())
                .extracting(DiscoverableApplicationFoundEvent::getFoundApp)
                .extracting(DiscoverableApplication::getName)
                .containsExactlyInAnyOrderElementsOf(expectedApplicationsNames);
    }

    @Test
    void onApplicationReadyEvent_ignoresAlreadyCachedAps() {
        assertKnownAppsIgnoredOn(EurekaApplicationCollector::onApplicationReadyEvent);
    }

    @Test
    void onCacheRefreshedEvent_ignoresAlreadyCachedAps() {
        assertKnownAppsIgnoredOn(EurekaApplicationCollector::onCacheRefreshedEvent);
    }

    private void assertKnownAppsIgnoredOn(Consumer<EurekaApplicationCollector> refreshingMethod) {
        String appName = "some-app";
        Application someApp = new Application(appName);
        EurekaDiscoverableApplication discoverableSomeApp = new EurekaDiscoverableApplication(someApp);

        EurekaApplicationCollector collector =
                new EurekaApplicationCollector(eurekaClientMock, Collections.emptyList(), eventPublisherMock);
        assumeThat(collector.getCollectedApplications()).isEmpty();

        addApp(collector, discoverableSomeApp);
        assumeThat(collector.getCollectedApplications()).hasSize(1);
        assumeThat(collector.getCollectedApplications()).allMatch(app -> app.getName().equals(appName));

        given(eurekaClientMock.getApplications().getRegisteredApplications()).willReturn(List.of(someApp));

        refreshingMethod.accept(collector);

        assertThat(collector.getCollectedApplications()).hasSize(1);
        assertThat(collector.getCollectedApplications()).allMatch(app -> app.getName().equals(appName));
        then(eventPublisherMock).shouldHaveNoInteractions();
    }

    @Test
    void onApplicationReadyEvent_cacheClearedOfStaleApps() {
        assertCacheClearedOfStaleAppsOn(EurekaApplicationCollector::onApplicationReadyEvent);
    }

    @Test
    void onCacheRefreshedEvent_cacheClearedOfStaleApps() {
        assertCacheClearedOfStaleAppsOn(EurekaApplicationCollector::onCacheRefreshedEvent);
    }

    private void assertCacheClearedOfStaleAppsOn(Consumer<EurekaApplicationCollector> refreshingMethod) {
        EurekaApplicationCollector collector =
                new EurekaApplicationCollector(eurekaClientMock, Collections.emptyList(), eventPublisherMock);
        EurekaDiscoverableApplication appMock = mock(EurekaDiscoverableApplication.class);
        String appName = "some-app";
        given(appMock.getName()).willReturn(appName);
        assumeThat(collector.getCollectedApplications()).isEmpty();
        addApp(collector, appMock);
        assumeThat(collector.getCollectedApplications()).hasSize(1);
        assumeThat(collector.getCollectedApplications()).allMatch(app -> app.getName().equals(appName));

        given(eurekaClientMock.getApplications().getRegisteredApplications()).willReturn(Collections.emptyList());

        refreshingMethod.accept(collector);

        assertThat(collector.getCollectedApplications()).isEmpty();
        ArgumentCaptor<DiscoverableApplicationLostEvent> eventCaptor = ArgumentCaptor.forClass(DiscoverableApplicationLostEvent.class);
        then(eventPublisherMock).should().publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue())
                .extracting(DiscoverableApplicationLostEvent::getLostApp)
                .extracting(DiscoverableApplication::getName)
                .isEqualTo(appName);
    }

    @SuppressWarnings("unchecked")
    @SneakyThrows
    private static void addApp(EurekaApplicationCollector collector, EurekaDiscoverableApplication app) {
        String applicationFieldName = "discoveredApplications";
        Field applicationsField = EurekaApplicationCollector.class
                .getDeclaredField(applicationFieldName);
        assumeThat(applicationsField)
                .withFailMessage(MessageFormat.format(
                        "No such field in {0}: {1}",
                        EurekaApplicationCollector.class.getSimpleName(),
                        applicationFieldName))
                .isNotNull();
        applicationsField.setAccessible(true);
        Set<EurekaDiscoverableApplication> applications =
                (Set<EurekaDiscoverableApplication>) applicationsField.get(collector);
        applications.add(app);
    }
}