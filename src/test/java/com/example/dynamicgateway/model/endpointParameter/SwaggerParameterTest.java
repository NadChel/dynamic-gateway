package com.example.dynamicgateway.model.endpointParameter;

import io.swagger.v3.oas.models.parameters.Parameter;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class SwaggerParameterTest {
    @Test
    void ifNullStringPassed_setsWrappedParameterToDefaultInstance() {
        SwaggerParameter parameter = new SwaggerParameter((String) null);
        assertThat(parameter).extracting(this::unwrap).isNotNull();
        assertThat(parameter.isRequired()).isFalse();
    }

    private Parameter unwrap(SwaggerParameter swaggerParameter) {
        return (Parameter) ReflectionTestUtils.getField(swaggerParameter, "nativeParameter");
    }

    @Test
    void ifNullStringPassed_parameterNameIsEmptyString() {
        SwaggerParameter parameter = new SwaggerParameter((String) null);
        assertThat(parameter.getName()).isEmpty();
    }

    @Test
    void ifNonNullStringPassed_wrappedParameterSetToPassedValue() {
        String paramName = "some-param";
        SwaggerParameter parameter = new SwaggerParameter(paramName);
        assertThat(parameter.getName()).isEqualTo(paramName);
    }

    @Test
    void ifNonNullStringPassed_parameterIsConsideredNotRequired() {
        String paramName = "some-param";
        SwaggerParameter parameter = new SwaggerParameter(paramName);
        assertThat(parameter.isRequired()).isEqualTo(false);
    }

    @Test
    void ifNullNativeParameterPassed_setsWrappedParameterToDefaultInstance() {
        SwaggerParameter parameter = new SwaggerParameter((Parameter) null);
        assertThat(parameter).extracting(this::unwrap).isNotNull();
        assertThat(parameter.isRequired()).isFalse();
    }

    @Test
    void ifNonNullNativeParameterPassed_setsWrappedParameterToPassedValue() {
        SwaggerParameter requiredParam = new SwaggerParameter(new Parameter().required(true));
        assertThat(requiredParam).extracting(this::unwrap).isNotNull();
        assertThat(requiredParam.isRequired()).isTrue();

        SwaggerParameter optionalParam = new SwaggerParameter(new Parameter().required(false));
        assertThat(optionalParam).extracting(this::unwrap).isNotNull();
        assertThat(optionalParam.isRequired()).isFalse();
    }
}
