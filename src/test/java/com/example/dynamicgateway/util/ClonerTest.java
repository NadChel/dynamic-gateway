package com.example.dynamicgateway.util;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assumptions.assumeThat;

class ClonerTest {
    @Test
    void deepCopy_returnsDeepCopy_onValidObject() {
        String oldValue = "some string";
        SomeDependency someDependency = new SomeDependency(oldValue);
        SomeClass someClass = new SomeClass(someDependency);

        SomeClass someClassCopy = Cloner.deepCopy(someClass, SomeClass.class);

        String newValue = "some other string";
        someClassCopy.getSomeDependency().setSomeString(newValue);
        assumeThat(someClassCopy.getSomeDependency().getSomeString()).isEqualTo(newValue);
        assertThat(someClass.getSomeDependency().getSomeString()).isEqualTo(oldValue);
    }

    @Test
    void deepCopy_throwsJsonProcessingException_onInvalidObject() {
        SomeClassWithNoExposedCreationMechanism invalidObject = new SomeClassWithNoExposedCreationMechanism();
        assertThatThrownBy(() -> Cloner.deepCopy(invalidObject, SomeClassWithNoExposedCreationMechanism.class))
                .isInstanceOf(JsonProcessingException.class);
    }

    @Getter
    static class SomeClass {
        SomeDependency someDependency;

        @JsonCreator
        public SomeClass(SomeDependency someDependency) {
            this.someDependency = someDependency;
        }
    }

    @Getter
    @Setter
    static class SomeDependency {
        String someString;

        @JsonCreator
        public SomeDependency(String someString) {
            this.someString = someString;
        }
    }

    static class SomeClassWithNoExposedCreationMechanism {
        private SomeClassWithNoExposedCreationMechanism() {}
    }
}