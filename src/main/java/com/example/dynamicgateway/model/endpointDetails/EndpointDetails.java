package com.example.dynamicgateway.model.endpointDetails;

import com.example.dynamicgateway.model.endpointParameter.EndpointParameter;
import com.example.dynamicgateway.model.endpointRequestBody.EndpointRequestBody;
import org.springframework.http.HttpMethod;

import java.util.List;

public interface EndpointDetails {
    String getPath();

    HttpMethod getMethod();

    List<String> getTags();

    List<? extends EndpointParameter> getParameters();

    EndpointRequestBody getRequestBody();
}
