package com.example.dynamicgateway.testModel;

import com.example.dynamicgateway.model.discoverableApplication.DiscoverableApplication;
import com.example.dynamicgateway.model.discoverableApplication.EurekaDiscoverableApplication;
import com.example.dynamicgateway.model.documentedApplication.SwaggerApplication;
import com.example.dynamicgateway.model.documentedEndpoint.SwaggerEndpoint;
import com.example.dynamicgateway.model.endpointDetails.SwaggerEndpointDetails;
import com.example.dynamicgateway.testUtil.SwaggerParseResultGenerator;
import com.netflix.discovery.shared.Application;
import lombok.Getter;
import org.springframework.http.HttpMethod;

public class SwaggerEndpointStub extends SwaggerEndpoint {
    private SwaggerEndpointStub(Builder builder) {
        super(builder.getDeclaringApp(), builder.getDetailsBuilder().build());
    }

    public static Builder builder() {
        return new Builder();
    }

    @Getter
    public static class Builder {
        private final SwaggerApplication declaringApp;
        private final SwaggerEndpointDetails.Builder detailsBuilder = SwaggerEndpointDetails.builder();

        private Builder() {
            Application eurekaApplication = new Application("test-app");
            DiscoverableApplication<Application> discoverableApplication = new EurekaDiscoverableApplication(eurekaApplication);
            declaringApp = new SwaggerApplication(discoverableApplication, SwaggerParseResultGenerator.empty());
        }

        public Builder declaringAppName(String name) {
            ((Application) declaringApp.getDiscoverableApp().unwrap()).setName(name);
            return this;
        }

        public Builder method(HttpMethod method) {
            detailsBuilder.method(method);
            return this;
        }

        public Builder path(String path) {
            detailsBuilder.path(path);
            return this;
        }

        public SwaggerEndpointStub build() {
            return new SwaggerEndpointStub(this);
        }
    }
}
