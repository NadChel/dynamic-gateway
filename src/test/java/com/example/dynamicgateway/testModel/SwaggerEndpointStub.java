package com.example.dynamicgateway.testModel;

import com.example.dynamicgateway.model.documentedApplication.SwaggerApplication;
import com.example.dynamicgateway.model.documentedEndpoint.SwaggerEndpoint;
import com.example.dynamicgateway.model.endpointDetails.SwaggerEndpointDetails;
import io.swagger.v3.oas.models.PathItem;
import lombok.Getter;
import lombok.Setter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Getter
@Setter
public class SwaggerEndpointStub extends SwaggerEndpoint {
    private SwaggerEndpointStub(Builder builder) {
        super(builder.getAppMock(), builder.getDetailsBuilder().build());
    }

    public static Builder builder() {
        return new Builder();
    }

    @Getter
    public static class Builder {
        private final SwaggerApplication appMock;
        private final SwaggerEndpointDetails.Builder detailsBuilder = SwaggerEndpointDetails.builder();

        private Builder() {
            appMock = mock(SwaggerApplication.class);
            when(appMock.getName()).thenReturn("test-application");
        }

        public Builder method(PathItem.HttpMethod method) {
            detailsBuilder.setMethod(method);
            return this;
        }

        public Builder path(String path) {
            detailsBuilder.setPath(path);
            return this;
        }

        public SwaggerEndpointStub build() {
            return new SwaggerEndpointStub(this);
        }

        public String toString() {
            return "SwaggerEndpointStub.SwaggerEndpointStubBuilder(method=" + detailsBuilder.getMethod() + ", path=" + detailsBuilder.getPath() + ")";
        }
    }
}
