package com.example.dynamicgateway.model.documentedEndpoint;

import com.example.dynamicgateway.model.documentedApplication.SwaggerApplication;
import com.example.dynamicgateway.model.endpointDetails.SwaggerEndpointDetails;
import lombok.Getter;

import java.text.MessageFormat;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * A {@link DocumentedEndpoint} exposed by a {@link SwaggerApplication}
 */
@Getter
public class SwaggerEndpoint implements DocumentedEndpoint<SwaggerApplication> {
    private final SwaggerApplication declaringApp;
    private final SwaggerEndpointDetails details;

    public SwaggerEndpoint(SwaggerApplication declaringApp, SwaggerEndpointDetails details) {
        Stream.of(declaringApp, details).forEach(Objects::requireNonNull);
        this.declaringApp = declaringApp;
        this.details = details;
    }

    @Override
    public String toString() {
        return MessageFormat.format(
                "{0} {1} {2}",
                declaringApp.getName(),
                details.getMethod(),
                details.getPath()
        );
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SwaggerEndpoint otherEndpoint)) return false;

        return declaringApp.getName().equals(otherEndpoint.getDeclaringApp().getName()) &&
                Objects.equals(details.getMethod(), otherEndpoint.getDetails().getMethod()) &&
                Objects.equals(details.getPath(), otherEndpoint.getDetails().getPath());
    }

    @Override
    public final int hashCode() {
        int result = declaringApp.getName().hashCode();
        result = 31 * result + details.getMethod().hashCode();
        result = 31 * result + details.getPath().hashCode();
        return result;
    }
}
