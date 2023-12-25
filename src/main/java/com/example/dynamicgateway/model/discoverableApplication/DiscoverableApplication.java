package by.afinny.apigateway.model.discoverableApplication;

/**
 * Application whose instances can be located by means of a discovery service
 */
public interface DiscoverableApplication {
    String getName();

    String getDiscoveryServiceScheme();
}
