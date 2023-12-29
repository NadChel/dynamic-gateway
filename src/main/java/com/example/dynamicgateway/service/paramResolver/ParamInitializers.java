package com.example.dynamicgateway.service.paramResolver;

import com.example.dynamicgateway.model.endpointParameter.EndpointParameter;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class ParamInitializers {
    private final Map<String, ParamInitializer> paramToParamInitializerMap;

    public ParamInitializers(List<ParamInitializer> paramInitializers) {
        HashMap<String, ParamInitializer> paramToParamInitializerMap = new HashMap<>();
        for (ParamInitializer paramInitializer : paramInitializers) {
            paramToParamInitializerMap.put(paramInitializer.getInitializedParam(), paramInitializer);
        }
        this.paramToParamInitializerMap = paramToParamInitializerMap;
    }

    public Optional<ParamInitializer> findInitializerForParam(EndpointParameter param) {
        return Optional.ofNullable(paramToParamInitializerMap.get(param.getName()));
    }
}
