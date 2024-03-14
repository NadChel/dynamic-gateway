package com.example.dynamicgateway.service.paramInitializer;

/**
 * A {@link ParamInitializer} that employs the {@link ParamInitializingStrategy#REPLACE_PARAMS_ALWAYS} strategy
 */
public abstract class ReplacingParamInitializer implements ParamInitializer {
    @Override
    public ParamInitializingStrategy getParamStrategy() {
        return ParamInitializingStrategy.REPLACE_PARAMS_ALWAYS;
    }
}
