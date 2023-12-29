package com.example.dynamicgateway.model.endpointDetails;

import com.example.dynamicgateway.model.endpointParameter.SwaggerParameter;
import com.example.dynamicgateway.model.endpointRequestBody.SwaggerRequestBody;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import lombok.Getter;
import org.springframework.http.HttpMethod;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

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
        private String path;
        private HttpMethod method;
        private List<SwaggerParameter> parameters;
        private SwaggerRequestBody requestBody;
        private List<String> tags;

        Builder() {
        }

        public Builder setPath(String path) {
            this.path = isNullOrBlank(path) ? "/" : path;
            return this;
        }

        private boolean isNullOrBlank(String path) {
            return path == null || path.isBlank();
        }

        public Builder setMethod(PathItem.HttpMethod method) {
            this.method = (method == null) ?
                    HttpMethod.GET :
                    HttpMethod.valueOf(method.toString());
            return this;
        }

        public Builder setParameters(List<Parameter> parameters) {
            this.parameters = isNullOrEmpty(parameters) ?
                    Collections.emptyList() :
                    parameters.stream().map(SwaggerParameter::new).toList();
            return this;
        }

        private boolean isNullOrEmpty(Collection<?> collection) {
            return collection == null || collection.isEmpty();
        }

        public Builder setRequestBody(RequestBody requestBody) {
            this.requestBody = (requestBody == null) ?
                    SwaggerRequestBody.empty() :
                    new SwaggerRequestBody(requestBody);
            return this;
        }

        public Builder setTags(List<String> tags) {
            this.tags = isNullOrEmpty(tags) ?
                    Collections.emptyList() :
                    tags;
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
