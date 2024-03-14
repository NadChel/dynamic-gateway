package com.example.dynamicgateway.service.paramInitializer;

import com.example.dynamicgateway.model.endpointParameter.EndpointParameter;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A wrapper around a {@code Map} of request parameter names to {@link ParamInitializer}s
 * that are capable of providing such parameters
 */
@Component
public class ParamInitializers {
    private final Map<String, ParamInitializer> paramToParamInitializerMap;

    /**
     * Creates a new instance of {@code ParamInitializers} from a {@code List} of
     * provided {@code ParamInitializer}s. Note that in case more than one {@code ParamInitializer}
     * handles a given parameter, say {@code "param-one"}, only the last handler of that parameter
     * in the list will be returned by {@code findInitializerForParam("param-one")}
     */
    public ParamInitializers(List<ParamInitializer> paramInitializers) {
        HashMap<String, ParamInitializer> paramToParamInitializerMap = new HashMap<>();
        for (ParamInitializer paramInitializer : paramInitializers) {
            paramToParamInitializerMap.put(paramInitializer.getParamName(), paramInitializer);
        }
        this.paramToParamInitializerMap = paramToParamInitializerMap;
    }

    /**
     * A shorthand for {@code findInitializerForParam(endpointParameter.getName())}
     *
     * @see ParamInitializers#findInitializerForParam(String)
     */
    public Optional<ParamInitializer> findInitializerForParam(EndpointParameter param) {
        return findInitializerForParam(param.getName());
    }

    /**
     * Attempts to find a {@code ParamInitializer} handling the provided parameter
     *
     * @param paramName the name of a request parameter
     * @return an {@code Optional} of a {@code ParamInitializer} whose {@link ParamInitializer#getParamName()}
     * equals the passed-in parameter name (considering case); if such a {@code ParamInitializer}
     * wasn't found among injected {@code ParamInitializer}s, the method returns an empty {@code Optional}
     */
    public Optional<ParamInitializer> findInitializerForParam(String paramName) {
        return Optional.ofNullable(paramToParamInitializerMap.get(paramName));
    }
}
