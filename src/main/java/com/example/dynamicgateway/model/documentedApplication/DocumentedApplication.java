package by.afinny.apigateway.model.documentedApplication;

import by.afinny.apigateway.model.discoverableApplication.DiscoverableApplication;
import by.afinny.apigateway.model.documentedEndpoint.DocumentedEndpoint;

import java.util.List;

/**
 * Application that exposes its API documentation, including available endpoints
 *
 * @param <T> type of document object of this {@code DocumentedApplication}
 */
public interface DocumentedApplication<T> {
    DiscoverableApplication getDiscoverableApp();

    String getDescription();

    List<? extends DocumentedEndpoint<?>> getEndpoints();

    T getNativeDoc();

    default String getName() {
        return getDiscoverableApp().getName();
    }
}
