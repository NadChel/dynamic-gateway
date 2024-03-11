package com.example.dynamicgateway.model.discoverableApplication;

import com.netflix.discovery.shared.Application;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EurekaDiscoverableApplicationTest {
    @Test
    @SuppressWarnings("DataFlowIssue")
    void ifNullPassedIntoConstructor_throws() {
        assertThatThrownBy(() -> new EurekaDiscoverableApplication(null));
    }

    @Test
    void equalsHashCodeContract() {
        EqualsVerifier.forClass(EurekaDiscoverableApplication.class)
                .withPrefabValues(Application.class,
                        new Application("app-1"),
                        new Application("app-2"))
                .withNonnullFields("eurekaApplication")
                .verify();
    }
}