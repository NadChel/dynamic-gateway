package com.example.dynamicgateway.model.discoverableApplication;

import com.netflix.discovery.shared.Application;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

/**
 * {@link DiscoverableApplication} that wraps a Eureka-registered {@link Application}
 */
@RequiredArgsConstructor
public class EurekaDiscoverableApplication implements DiscoverableApplication {
    public static final String LB_SCHEME = "lb://";
    private final Application eurekaApplication;

    @Override
    public String getName() {
        return eurekaApplication.getName();
    }

    @Override
    public String getDiscoveryServiceScheme() {
        return LB_SCHEME;
    }

    public static EurekaDiscoverableApplication from(Application application) {
        return new EurekaDiscoverableApplication(application);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EurekaDiscoverableApplication that)) return false;
        return eurekaApplication.getName().equals(that.eurekaApplication.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(eurekaApplication.getName());
    }
}
