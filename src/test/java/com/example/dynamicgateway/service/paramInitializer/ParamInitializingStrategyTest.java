package com.example.dynamicgateway.service.paramInitializer;

import org.junit.jupiter.api.Test;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ParamInitializingStrategyTest {
    @Test
    void replaceParamAlways_ifParamsAbsent_replacesParams() {
        ParamInitializingStrategyAssert
                .assertThatStrategy(ParamInitializingStrategy.REPLACE_PARAMS_ALWAYS)
                .withAbsentInitialParamsOfName()
                .replacesParams();
    }

    @Test
    void replaceParamAlways_ifParamsPresent_replacesParams() {
        ParamInitializingStrategyAssert
                .assertThatStrategy(ParamInitializingStrategy.REPLACE_PARAMS_ALWAYS)
                .withPresentInitialParamsOfName()
                .replacesParams();
    }

    @Test
    void replaceParamIfAbsent_ifAbsent_replacesParams() {
        ParamInitializingStrategyAssert
                .assertThatStrategy(ParamInitializingStrategy.REPLACE_PARAMS_IF_ABSENT)
                .withAbsentInitialParamsOfName()
                .replacesParams();
    }

    @Test
    void replaceParamIfAbsent_ifPresent_keepsOldParams() {
        ParamInitializingStrategyAssert
                .assertThatStrategy(ParamInitializingStrategy.REPLACE_PARAMS_IF_ABSENT)
                .withPresentInitialParamsOfName()
                .keepsOldParams();
    }

    @Test
    void appendParamsAlways_ifParamsAbsent_appendsParams() {
        ParamInitializingStrategyAssert
                .assertThatStrategy(ParamInitializingStrategy.APPEND_PARAMS_ALWAYS)
                .withAbsentInitialParamsOfName()
                .appendsParams();
    }

    @Test
    void appendParamsAlways_ifParamsPresent_appendsParams() {
        ParamInitializingStrategyAssert
                .assertThatStrategy(ParamInitializingStrategy.APPEND_PARAMS_ALWAYS)
                .withPresentInitialParamsOfName()
                .appendsParams();
    }

    @Test
    void appendParamsIfAbsent_ifParamsAbsent_appendsParams() {
        ParamInitializingStrategyAssert
                .assertThatStrategy(ParamInitializingStrategy.APPEND_PARAMS_IF_ABSENT)
                .withAbsentInitialParamsOfName()
                .appendsParams();
    }

    @Test
    void appendParamsIfAbsent_ifParamsPresent_keepsOldParams() {
        ParamInitializingStrategyAssert
                .assertThatStrategy(ParamInitializingStrategy.APPEND_PARAMS_IF_ABSENT)
                .withPresentInitialParamsOfName()
                .keepsOldParams();
    }

    private static class ParamInitializingStrategyAssert {
        private final ParamInitializingStrategy testedStrategy;
        private List<String> initialParams = Collections.emptyList();
        private Expectation behaviorExpectation = Expectation.KEEPING_OLD_PARAMS;

        private ParamInitializingStrategyAssert(ParamInitializingStrategy testedStrategy) {
            this.testedStrategy = testedStrategy;
        }

        static ParamInitializingStrategyAssert assertThatStrategy(ParamInitializingStrategy testedStrategy) {
            return new ParamInitializingStrategyAssert(testedStrategy);
        }

        ParamInitializingStrategyAssert withAbsentInitialParamsOfName() {
            this.initialParams = Collections.emptyList();
            return this;
        }

        ParamInitializingStrategyAssert withPresentInitialParamsOfName() {
            this.initialParams = List.of(UUID.randomUUID().toString());
            return this;
        }

        void replacesParams() {
            this.behaviorExpectation = Expectation.REPLACEMENT;
            assertStrategyBehavior();
        }

        void appendsParams() {
            this.behaviorExpectation = Expectation.APPENDING;
            assertStrategyBehavior();
        }

        void keepsOldParams() {
            this.behaviorExpectation = Expectation.KEEPING_OLD_PARAMS;
            assertStrategyBehavior();
        }

        void assertStrategyBehavior() {
            String paramName = "param";
            MultiValueMap<String, String> queryMap = new LinkedMultiValueMap<>(Map.of(
                    paramName, initialParams
            ));
            List<String> newParams = List.of(UUID.randomUUID().toString());
            MultiValueMap<String, String> newQueryMap = testedStrategy.apply(queryMap, paramName, newParams);
            List<String> expectedParamValues =
                    behaviorExpectation.getExpectedParamValues(initialParams, newParams);
            assertThat(newQueryMap.get(paramName))
                    .containsExactlyInAnyOrderElementsOf(expectedParamValues);
        }

        private enum Expectation {
            REPLACEMENT {
                @Override
                List<String> getExpectedParamValues(List<String> initialParamValues,
                                                    List<String> newParamValues) {
                    return newParamValues;
                }
            }, APPENDING {
                @Override
                List<String> getExpectedParamValues(List<String> initialParamValues,
                                                    List<String> newParamValues) {
                    return Stream.concat(initialParamValues.stream(),
                                    newParamValues.stream())
                            .toList();
                }
            },
            KEEPING_OLD_PARAMS {
                @Override
                List<String> getExpectedParamValues(List<String> initialParamValues,
                                                    List<String> newParamValues) {
                    return initialParamValues;
                }
            };

            abstract List<String> getExpectedParamValues(List<String> initialParams,
                                                         List<String> newParams);
        }
    }
}