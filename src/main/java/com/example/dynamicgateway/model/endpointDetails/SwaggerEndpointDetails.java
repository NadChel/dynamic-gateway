package com.example.dynamicgateway.model.endpointDetails;

import com.example.dynamicgateway.model.endpointParameter.EndpointParameter;
import com.example.dynamicgateway.model.endpointParameter.SwaggerParameter;
import com.example.dynamicgateway.model.endpointRequestBody.SwaggerRequestBody;
import com.example.dynamicgateway.util.UriValidator;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import lombok.Getter;
import org.springframework.http.HttpMethod;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

public class SwaggerEndpointDetails implements EndpointDetails {
    @Getter
    private final String path;
    @Getter
    private final HttpMethod method;
    private final List<SwaggerParameter> parameters;
    @Getter
    private final SwaggerRequestBody requestBody;
    private final List<String> tags;

    SwaggerEndpointDetails(Builder builder) {
        this.path = builder.getPath();
        this.method = builder.getMethod();
        this.parameters = builder.getParameters();
        this.requestBody = builder.getRequestBody();
        this.tags = builder.getTags();
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public List<String> getTags() {
        return List.copyOf(tags);
    }

    @Override
    public List<? extends EndpointParameter> getParameters() {
        return List.copyOf(parameters);
    }

    @Getter
    public static class Builder {
        private String path = "/";
        private HttpMethod method = HttpMethod.GET;
        private List<SwaggerParameter> parameters = new CopyOnWriteArrayList<>();
        private SwaggerRequestBody requestBody = SwaggerRequestBody.empty();
        private List<String> tags = new CopyOnWriteArrayList<>();

        private Builder() {
        }

        public Builder path(String path) {
            UriValidator.requireValidPath(path);
            this.path = path;
            return this;
        }

        public Builder method(PathItem.HttpMethod method) {
            Objects.requireNonNull(method);
            this.method = HttpMethod.valueOf(method.name());
            return this;
        }

        public Builder method(HttpMethod method) {
            Objects.requireNonNull(method);
            this.method = method;
            return this;
        }

        public Builder parameters(List<Parameter> parameters) {
            if (parameters != null) {
                this.parameters = parameters.stream().map(SwaggerParameter::new).toList();
            }
            return this;
        }

        public Builder requestBody(RequestBody requestBody) {
            if (requestBody != null) {
                this.requestBody = new SwaggerRequestBody(requestBody);
            }
            return this;
        }

        public Builder tags(List<String> tags) {
            if (tags != null) {
                this.tags = tags;
            }
            return this;
        }

        public SwaggerEndpointDetails build() {
            return new SwaggerEndpointDetails(this);
        }
    }
}
