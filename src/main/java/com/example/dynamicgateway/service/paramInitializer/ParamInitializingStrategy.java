package com.example.dynamicgateway.service.paramInitializer;

import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.stream.Stream;

/**
 * A strategy for mutating query parameter maps
 */
public enum ParamInitializingStrategy {
    /**
     * A strategy that always replaces old parameter values with new values
     */
    REPLACE_PARAMS_ALWAYS {
        @Override
        MultiValueMap<String, String> apply(MultiValueMap<String, String> queryMap,
                                            String paramName, List<String> newParamValues) {
            return queryMapWithReplacedParams(queryMap, paramName, newParamValues);
        }
    },
    /**
     * A strategy that replaces old parameter values with new values, but only if
     * no parameter value is already associated with the parameter name key
     */
    REPLACE_PARAMS_IF_ABSENT {
        @Override
        MultiValueMap<String, String> apply(MultiValueMap<String, String> queryMap,
                                            String paramName, List<String> newParamValues) {
            List<String> currentParamValues = queryMap.get(paramName);
            return CollectionUtils.isEmpty(currentParamValues) ?
                    queryMapWithReplacedParams(queryMap, paramName, newParamValues) :
                    queryMap;
        }
    },
    /**
     * A strategy that always adds provided parameter values to those already associated
     * with the parameter name key (if any)
     */
    APPEND_PARAMS_ALWAYS {
        @Override
        MultiValueMap<String, String> apply(MultiValueMap<String, String> queryMap,
                                            String paramName, List<String> newParamValues) {
            return queryMapWithAppendedParams(queryMap, paramName, newParamValues);
        }
    },
    /**
     * A strategy that adds provided parameter values to the query map, but only if
     * no parameter value is already associated with the parameter name key
     */
    APPEND_PARAMS_IF_ABSENT {
        @Override
        MultiValueMap<String, String> apply(MultiValueMap<String, String> queryMap,
                                            String paramName, List<String> newParamValues) {
            List<String> currentParamValues = queryMap.get(paramName);
            return CollectionUtils.isEmpty(currentParamValues) ?
                    queryMapWithAppendedParams(queryMap, paramName, newParamValues) :
                    queryMap;
        }
    };

    /**
     * Applies this strategy
     *
     * @param queryMap       a map of request parameter names to associated parameter values
     * @param paramName      the name of a parameter
     * @param newParamValues new values for the parameter
     * @return a query map, potentially enriched with new parameter values
     */
    abstract MultiValueMap<String, String> apply(MultiValueMap<String, String> queryMap,
                                                 String paramName, List<String> newParamValues);

    private static MultiValueMap<String, String> queryMapWithReplacedParams(MultiValueMap<String, String> queryMap,
                                                                            String paramName, List<String> newParamValues) {
        return queryMapWithNewParamValues(queryMap, paramName, newParamValues);
    }

    private static MultiValueMap<String, String> queryMapWithAppendedParams(MultiValueMap<String, String> queryMap,
                                                                            String paramName, List<String> newParamValues) {
        List<String> currentParamValues = queryMap.get(paramName);
        List<String> aggregatedParamValues = Stream.concat(
                        currentParamValues.stream(),
                        newParamValues.stream())
                .toList();
        return queryMapWithNewParamValues(queryMap, paramName, aggregatedParamValues);
    }

    private static MultiValueMap<String, String> queryMapWithNewParamValues(MultiValueMap<String, String> queryMap,
                                                                            String paramName, List<String> newParamValues) {
        MultiValueMap<String, String> writableQueryMap = new LinkedMultiValueMap<>(queryMap);
        writableQueryMap.put(paramName, newParamValues);
        return writableQueryMap;
    }
}
