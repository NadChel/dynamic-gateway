package com.example.dynamicgateway.model.discoverableApplication;

import com.example.dynamicgateway.config.annotation.ExcludeFromJacocoGeneratedReport;
import com.netflix.discovery.shared.Application;
import org.springframework.lang.NonNull;

import java.util.Objects;

/**
 * A {@link DiscoverableApplication} that wraps a Eureka-registered {@link Application}
 */
public class EurekaDiscoverableApplication implements DiscoverableApplication<Application> {
    public static final String LB_SCHEME = "lb://";
    private final Application eurekaApplication;

    public EurekaDiscoverableApplication(@NonNull Application eurekaApplication) {
        Objects.requireNonNull(eurekaApplication);
        this.eurekaApplication = eurekaApplication;
    }

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
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EurekaDiscoverableApplication that)) return false;
        return getName().equals(that.getName());
    }

    @Override
    public final int hashCode() {
        return Objects.hash(getName());
    }

    @Override
    @ExcludeFromJacocoGeneratedReport
    public String toString() {
        return getName();
    }
}
