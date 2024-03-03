package com.example.dynamicgateway.util;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;

class ClonerTest {
    @Test
    void testDeepCopy() {
        String oldValue = "some string";
        SomeDependency someDependency = new SomeDependency(oldValue);
        SomeClass someClass = new SomeClass(someDependency);

        SomeClass someClassCopy = Cloner.deepCopy(someClass, SomeClass.class);

        String newValue = "some other string";
        someClassCopy.getSomeDependency().setSomeString(newValue);
        assumeThat(someClassCopy.getSomeDependency().getSomeString()).isEqualTo(newValue);
        assertThat(someClass.getSomeDependency().getSomeString()).isEqualTo(oldValue);
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
}