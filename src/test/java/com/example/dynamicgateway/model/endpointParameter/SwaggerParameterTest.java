package com.example.dynamicgateway.model.endpointParameter;

import io.swagger.v3.oas.models.parameters.Parameter;
import org.junit.jupiter.api.Test;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;

class SwaggerParameterTest {
    @Test
    void ifNullStringPassedToConstructor_setsWrappedParameterToDefaultInstance() {
        SwaggerParameter parameter = new SwaggerParameter((String) null);
        assertThat(parameter).extracting(this::unwrap).isNotNull();
        assertThat(parameter.isRequired()).isFalse();
    }

    @SuppressWarnings("DataFlowIssue")
    private Parameter unwrap(SwaggerParameter swaggerParameter) {
        Field nativeParameterField =
                ReflectionUtils.findField(SwaggerParameter.class, "nativeParameter");
        assumeThat(nativeParameterField).isNotNull();
        nativeParameterField.setAccessible(true);
        return (Parameter) ReflectionUtils.getField(nativeParameterField, swaggerParameter);
    }

    @Test
    void ifNullStringPassedToConstructor_parameterNameIsEmptyString() {
        SwaggerParameter parameter = new SwaggerParameter((String) null);
        assertThat(parameter.getName()).isEmpty();
    }

    @Test
    void ifNonNullStringPassedToConstructor_wrappedParameterSetToPassedValue() {
        String paramName = "some-param";
        SwaggerParameter parameter = new SwaggerParameter(paramName);
        assertThat(parameter.getName()).isEqualTo(paramName);
    }

    @Test
    void ifNonNullStringPassedToConstructor_parameterIsConsideredNotRequired() {
        String paramName = "some-param";
        SwaggerParameter parameter = new SwaggerParameter(paramName);
        assertThat(parameter.isRequired()).isEqualTo(false);
    }

    @Test
    void ifNullNativeParameterPassedToConstructor_setsWrappedParameterToDefaultInstance() {
        SwaggerParameter parameter = new SwaggerParameter((Parameter) null);
        assertThat(parameter).extracting(this::unwrap).isNotNull();
        assertThat(parameter.isRequired()).isFalse();
    }

    @Test
    void ifNonNullNativeParameterPassedToConstructor_setsWrappedParameterToPassedValue() {
        SwaggerParameter requiredParam = new SwaggerParameter(new Parameter().required(true));
        assertThat(requiredParam).extracting(this::unwrap).isNotNull();
        assertThat(requiredParam.isRequired()).isTrue();

        SwaggerParameter optionalParam = new SwaggerParameter(new Parameter().required(false));
        assertThat(optionalParam).extracting(this::unwrap).isNotNull();
        assertThat(optionalParam.isRequired()).isFalse();
    }
}
