package by.afinny.apigateway.service.applicationCollector;

import by.afinny.apigateway.model.discoverableApplication.EurekaDiscoverableApplication;
import com.netflix.discovery.CacheRefreshedEvent;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.shared.Application;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * {@link ApplicationFinder} that relies on Netflix Eureka as its discovery service
 */
@Slf4j
@Component
public class EurekaApplicationFinder implements ApplicationFinder {
    private final EurekaClient eurekaClient;

    public EurekaApplicationFinder(EurekaClient eurekaClient, ApplicationEventPublisher publisher) {
        this.eurekaClient = eurekaClient;
        this.eurekaClient.registerEventListener(event -> {
            if (event instanceof CacheRefreshedEvent) {
                publisher.publishEvent(event);
            }
        });
    }

    @Override
    public Set<EurekaDiscoverableApplication> findOtherRegisteredApplications() {
        return eurekaClient.getApplications()
                .getRegisteredApplications()
                .stream()
                .filter(app -> !isSelf(app))
                .map(EurekaDiscoverableApplication::from)
                .collect(Collectors.toSet());
    }

    private boolean isSelf(Application application) {
        String nameOfThisApplication = eurekaClient.getApplicationInfoManager()
                .getInfo()
                .getAppName();
        return application.getName().equals(nameOfThisApplication);
    }
}
