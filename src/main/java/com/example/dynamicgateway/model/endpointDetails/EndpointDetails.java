package by.afinny.apigateway.model.endpointDetails;

import by.afinny.apigateway.model.endpointParameter.EndpointParameter;
import by.afinny.apigateway.model.endpointRequestBody.EndpointRequestBody;
import org.springframework.http.HttpMethod;

import java.util.List;

public interface EndpointDetails {
    String getPath();

    String getNonPrefixedPath();

    HttpMethod getMethod();

    List<String> getTags();

    List<? extends EndpointParameter> getParameters();

    EndpointRequestBody getRequestBody();
}
