package com.example.dynamicgateway.service.sieve;

import com.example.dynamicgateway.config.DiscoverableApplicationSieveConfig;
import com.example.dynamicgateway.model.discoverableApplication.DiscoverableApplication;
import com.netflix.discovery.EurekaClient;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

class DiscoverableApplicationSieveConfigTest {
    @Test
    void selfExclusionDiscoverableApplicationSieve() {
        String thisAppName = "gateway-app";
        EurekaClient eurekaClientMock = mock(EurekaClient.class, RETURNS_DEEP_STUBS);
        given(eurekaClientMock.getApplicationInfoManager().getInfo().getAppName()).willReturn(thisAppName);

        DiscoverableApplicationSieveConfig applicationSieveConfig = new DiscoverableApplicationSieveConfig();
        DiscoverableApplicationSieve sieve =
                applicationSieveConfig.selfExclusionDiscoverableApplicationSieve(eurekaClientMock);

        DiscoverableApplication<?> thisApp = mock(DiscoverableApplication.class);
        given(thisApp.getName()).willReturn(thisAppName);

        DiscoverableApplication<?> anotherApp = mock(DiscoverableApplication.class);
        given(anotherApp.getName()).willReturn("some-app");

        assertThat(sieve.isAllowed(thisApp)).isFalse();
        assertThat(sieve.isAllowed(anotherApp)).isTrue();
    }
}