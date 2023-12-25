package by.afinny.apigateway.model.documentedEndpoint;

import by.afinny.apigateway.model.documentedApplication.SwaggerApplication;
import by.afinny.apigateway.model.endpointDetails.EndpointDetails;
import lombok.Getter;

import java.text.MessageFormat;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * {@link DocumentedEndpoint} exposed by a {@link SwaggerApplication}
 */
public class SwaggerEndpoint implements DocumentedEndpoint<SwaggerApplication> {
    private final SwaggerApplication documentedApp;
    @Getter
    private final EndpointDetails details;

    public SwaggerEndpoint(SwaggerApplication documentedApp, EndpointDetails details) {
        Stream.of(documentedApp, details).forEach(Objects::requireNonNull);
        this.documentedApp = documentedApp;
        this.details = details;
    }

    @Override
    public SwaggerApplication getDeclaringApp() {
        return documentedApp;
    }

    @Override
    public String toString() {
        return MessageFormat.format(
                "{0} {1} {2}",
                documentedApp.getName(),
                details.getMethod(),
                details.getPath()
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SwaggerEndpoint otherEndpoint)) return false;

        return documentedApp.getName().equals(otherEndpoint.getDeclaringApp().getName()) &&
                details.getMethod().equals(otherEndpoint.getDetails().getMethod()) &&
                details.getPath().equals(otherEndpoint.getDetails().getPath());
    }

    @Override
    public int hashCode() {
        int result = documentedApp.getName().hashCode();
        result = 31 * result + details.getMethod().hashCode();
        result = 31 * result + details.getPath().hashCode();
        return result;
    }
}
