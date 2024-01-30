package com.example.dynamicgateway.model.endpointDetails;

import com.example.dynamicgateway.model.endpointParameter.SwaggerParameter;
import com.example.dynamicgateway.model.endpointRequestBody.SwaggerRequestBody;
import com.example.dynamicgateway.util.UriValidator;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import lombok.Getter;
import org.springframework.http.HttpMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
public class SwaggerEndpointDetails implements EndpointDetails {
    private final String path;
    private final HttpMethod method;
    private final List<SwaggerParameter> parameters;
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
    public String getNonPrefixedPath() {
        return path.startsWith("/auth") ?
                path.substring(getPrefix().length()) :
                path;
    }

    @Override
    public String getPrefix() {
        return path.startsWith("/auth") ?
                "/auth" :
                "";
    }

    @Getter
    public static class Builder {
        private String path = "/";
        private HttpMethod method = HttpMethod.GET;
        private List<SwaggerParameter> parameters = new ArrayList<>();
        private SwaggerRequestBody requestBody = SwaggerRequestBody.empty();
        private List<String> tags = new ArrayList<>();

        private Builder() {
        }

        public Builder setPath(String path) {
            UriValidator.requireValidPath(path);
            this.path = path;
            return this;
        }

        public Builder setMethod(PathItem.HttpMethod method) {
            Objects.requireNonNull(method);
            this.method = HttpMethod.valueOf(method.name());
            return this;
        }

        public Builder setMethod(HttpMethod method) {
            Objects.requireNonNull(method);
            this.method = method;
            return this;
        }

        public Builder setParameters(List<Parameter> parameters) {
            if (parameters != null) {
                this.parameters = parameters.stream().map(SwaggerParameter::new).toList();
            }
            return this;
        }

        public Builder setRequestBody(RequestBody requestBody) {
            if (requestBody != null) {
                this.requestBody = new SwaggerRequestBody(requestBody);
            }
            return this;
        }

        public Builder setTags(List<String> tags) {
            if (tags != null) {
                this.tags = tags;
            }
            return this;
        }

        public SwaggerEndpointDetails build() {
            return new SwaggerEndpointDetails(this);
        }

        public String toString() {
            return "SwaggerEndpointDetails.Builder(path=" + this.path + ", method=" + this.method + ", parameters=" + this.parameters + ", requestBody=" + this.requestBody + ", tags=" + this.tags + ")";
        }
    }
}
