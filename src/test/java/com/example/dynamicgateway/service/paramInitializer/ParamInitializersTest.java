package com.example.dynamicgateway.service.paramInitializer;

import com.example.dynamicgateway.model.endpointParameter.EndpointParameter;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.BDDMockito.given;

class ParamInitializersTest {
    @Test
    void findInitializerForParam_withSupportedParam() {
        ParamInitializer paramOneInitializerMock = mock(ParamInitializer.class);
        given(paramOneInitializerMock.getParamName()).willReturn("paramOne");

        ParamInitializer paramTwoInitializerMock = mock(ParamInitializer.class);
        given(paramTwoInitializerMock.getParamName()).willReturn("paramTwo");

        List<ParamInitializer> testParamInitializers = List.of(
                paramOneInitializerMock, paramTwoInitializerMock
        );

        EndpointParameter paramOneMock = mock(EndpointParameter.class);
        given(paramOneMock.getName()).willReturn("paramOne");

        ParamInitializers paramInitializers = new ParamInitializers(testParamInitializers);
        Optional<ParamInitializer> initializerForParam = paramInitializers.findInitializerForParam(paramOneMock);
        assertThat(initializerForParam).isPresent();
        assertThat(initializerForParam.orElseThrow()).isEqualTo(paramOneInitializerMock);
    }

    @Test
    void findInitializerForParam_withNonSupportedParam() {
        ParamInitializer paramOneInitializerMock = mock(ParamInitializer.class);
        given(paramOneInitializerMock.getParamName()).willReturn("paramOne");

        ParamInitializer paramTwoInitializerMock = mock(ParamInitializer.class);
        given(paramTwoInitializerMock.getParamName()).willReturn("paramTwo");

        List<ParamInitializer> testParamInitializers = List.of(
                paramOneInitializerMock, paramTwoInitializerMock
        );

        EndpointParameter paramOneMock = mock(EndpointParameter.class);
        given(paramOneMock.getName()).willReturn("paramThree");

        ParamInitializers paramInitializers = new ParamInitializers(testParamInitializers);
        Optional<ParamInitializer> initializerForParam = paramInitializers.findInitializerForParam(paramOneMock);
        assertThat(initializerForParam).isNotPresent();
    }
}