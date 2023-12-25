package by.afinny.apigateway.model.endpointDetails;

import by.afinny.apigateway.model.endpointParameter.EndpointParameter;
import by.afinny.apigateway.model.endpointParameter.SwaggerParameter;
import by.afinny.apigateway.model.endpointRequestBody.EndpointRequestBody;
import by.afinny.apigateway.model.endpointRequestBody.SwaggerRequestBody;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpMethod;

import java.util.List;

@Getter
@Builder(setterPrefix = "set")
public class SwaggerEndpointDetails implements EndpointDetails {
    private final String path;
    private final PathItem.HttpMethod method;
    private final List<Parameter> parameters;
    private final RequestBody requestBody;
    private final List<String> tags;

    @Override
    public HttpMethod getMethod() {
        return HttpMethod.valueOf(method.toString());
    }

    @Override
    public String getNonPrefixedPath() {
        return path.startsWith("/auth") ?
                path.substring(5) :
                path;
    }

    @Override
    public List<? extends EndpointParameter> getParameters() {
        return parameters.stream()
                .map(SwaggerParameter::new)
                .toList();
    }

    @Override
    public EndpointRequestBody getRequestBody() {
        return new SwaggerRequestBody(requestBody);
    }
}
