package by.afinny.apigateway.model.discoverableApplication;

import com.netflix.discovery.shared.Application;
import lombok.RequiredArgsConstructor;

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
}
