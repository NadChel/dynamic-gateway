package com.example.dynamicgateway.model.discoverableApplication;

import com.netflix.discovery.shared.Application;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

/**
 * {@link DiscoverableApplication} that wraps a Eureka-registered {@link Application}
 */
@RequiredArgsConstructor
public class EurekaDiscoverableApplication implements DiscoverableApplication<Application> {
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

    @Override
    public Application unwrap() {
        return eurekaApplication;
    }

    public static EurekaDiscoverableApplication from(Application application) {
        return new EurekaDiscoverableApplication(application);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EurekaDiscoverableApplication that)) return false;
        return getName().equals(that.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName());
    }

    @Override
    public String toString() {
        return getName();
    }
}
