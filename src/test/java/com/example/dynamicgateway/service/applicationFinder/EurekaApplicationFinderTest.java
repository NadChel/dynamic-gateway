package com.example.dynamicgateway.service.applicationFinder;

import com.example.dynamicgateway.model.discoverableApplication.EurekaDiscoverableApplication;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.shared.Application;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.COLLECTION;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EurekaApplicationFinderTest {
    @Test
    void testFindOtherRegisteredApplications() {
        Application gatewayFake = new Application("this-gateway");
        Application appFake = new Application("test-app-1");
        Application anotherAppFake = new Application("test-app-2");

        EurekaClient eurekaClientMock = mock(EurekaClient.class, RETURNS_DEEP_STUBS);
        when(eurekaClientMock.getApplications().getRegisteredApplications()).thenReturn(List.of(
                gatewayFake, appFake, anotherAppFake
        ));
        when(eurekaClientMock.getApplicationInfoManager().getInfo().getAppName()).thenReturn("this-gateway");

        ApplicationEventPublisher eventPublisherMock = mock(ApplicationEventPublisher.class);

        EurekaApplicationFinder eurekaApplicationFinder = new EurekaApplicationFinder(eurekaClientMock, eventPublisherMock);

        Set<EurekaDiscoverableApplication> actualApplications = eurekaApplicationFinder.findOtherRegisteredApplications();

        Set<EurekaDiscoverableApplication> expectedApplications = Stream.of(appFake, anotherAppFake)
                .map(EurekaDiscoverableApplication::from)
                .collect(Collectors.toSet());

        assertThat(actualApplications).asInstanceOf(COLLECTION).containsExactlyInAnyOrderElementsOf(expectedApplications);
    }
}